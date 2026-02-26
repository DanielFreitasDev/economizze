package io.nexus.economizze.transferencia.service;

import io.nexus.economizze.conta.model.Conta;
import io.nexus.economizze.conta.service.ContaService;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.shared.exception.RecursoNaoEncontradoException;
import io.nexus.economizze.shared.util.NumeroUtil;
import io.nexus.economizze.shared.util.TextoUtil;
import io.nexus.economizze.transferencia.dto.OcorrenciaTransferenciaDto;
import io.nexus.economizze.transferencia.dto.TransferenciaForm;
import io.nexus.economizze.transferencia.model.Transferencia;
import io.nexus.economizze.transferencia.repository.TransferenciaRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para transferencias entre contas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final ContaService contaService;

    /**
     * Lista transferencias base.
     */
    @Transactional(readOnly = true)
    public List<Transferencia> listar(boolean incluirInativos) {
        return incluirInativos ? transferenciaRepository.findAllByOrderByDataBaseDescDescricaoAsc() : transferenciaRepository.findAllByAtivoTrueOrderByDataBaseDescDescricaoAsc();
    }

    /**
     * Busca transferencia por id.
     */
    @Transactional(readOnly = true)
    public Transferencia buscarPorId(Long id) {
        return transferenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transferencia nao encontrada"));
    }

    /**
     * Cria transferencia com validacoes por tipo.
     */
    @Transactional
    public Transferencia criar(TransferenciaForm form) {
        validarFormulario(form);

        Transferencia transferencia = new Transferencia();
        preencherCampos(transferencia, form);

        Transferencia salva = transferenciaRepository.save(transferencia);
        log.info("Transferencia criada: {}", salva.getDescricao());
        return salva;
    }

    /**
     * Atualiza transferencia existente.
     */
    @Transactional
    public Transferencia atualizar(Long id, TransferenciaForm form) {
        validarFormulario(form);

        Transferencia transferencia = buscarPorId(id);
        preencherCampos(transferencia, form);

        Transferencia salva = transferenciaRepository.save(transferencia);
        log.info("Transferencia atualizada: {}", salva.getDescricao());
        return salva;
    }

    /**
     * Desativa transferencia base.
     */
    @Transactional
    public void desativar(Long id) {
        Transferencia transferencia = buscarPorId(id);
        transferencia.setAtivo(Boolean.FALSE);
        transferenciaRepository.save(transferencia);
        log.warn("Transferencia desativada: {}", transferencia.getDescricao());
    }

    /**
     * Exclui transferencia definitivamente.
     */
    @Transactional
    public void excluir(Long id) {
        Transferencia transferencia = buscarPorId(id);
        transferenciaRepository.delete(transferencia);
        log.warn("Transferencia excluida: {}", transferencia.getDescricao());
    }

    /**
     * Projeta ocorrencias de transferencias para a competencia informada.
     */
    @Transactional(readOnly = true)
    public List<OcorrenciaTransferenciaDto> listarOcorrenciasDaCompetencia(YearMonth competencia) {
        LocalDate fimCompetencia = competencia.atEndOfMonth();
        List<Transferencia> transferencias = transferenciaRepository
                .findAllByAtivoTrueAndDataBaseLessThanEqualOrderByDataBaseAscDescricaoAsc(fimCompetencia);

        List<OcorrenciaTransferenciaDto> ocorrencias = new ArrayList<>();
        for (Transferencia transferencia : transferencias) {
            gerarOcorrencia(transferencia, competencia).ifPresent(ocorrencias::add);
        }

        ocorrencias.sort(Comparator.comparing(OcorrenciaTransferenciaDto::dataOcorrencia)
                .thenComparing(OcorrenciaTransferenciaDto::descricao));
        return ocorrencias;
    }

    /**
     * Valida regras de transferencia por tipo e contas.
     */
    private void validarFormulario(TransferenciaForm form) {
        if (form.getContaOrigemId().equals(form.getContaDestinoId())) {
            throw new RegraDeNegocioException("Conta de origem e destino devem ser diferentes");
        }

        if (form.getTipo() == TipoLancamento.PARCELADO) {
            if (form.getTotalParcelas() == null || form.getTotalParcelas() < 2) {
                throw new RegraDeNegocioException("Transferencia parcelada exige total de parcelas >= 2");
            }
        }

        if (form.getTipo() == TipoLancamento.RECORRENTE && form.getDataFimRecorrencia() != null
                && form.getDataFimRecorrencia().isBefore(form.getDataBase())) {
            throw new RegraDeNegocioException("Data fim da recorrencia nao pode ser menor que data base");
        }

        if (form.getTipo() == TipoLancamento.AVULSO) {
            form.setDataFimRecorrencia(null);
            form.setTotalParcelas(null);
        }

        if (form.getTipo() == TipoLancamento.RECORRENTE) {
            form.setTotalParcelas(null);
        }

        if (form.getTipo() == TipoLancamento.PARCELADO) {
            form.setDataFimRecorrencia(null);
        }
    }

    /**
     * Preenche entidade a partir do formulario validado.
     */
    private void preencherCampos(Transferencia transferencia, TransferenciaForm form) {
        Conta contaOrigem = contaService.buscarPorId(form.getContaOrigemId());
        Conta contaDestino = contaService.buscarPorId(form.getContaDestinoId());

        transferencia.setDescricao(TextoUtil.paraMaiusculo(form.getDescricao()));
        transferencia.setContaOrigem(contaOrigem);
        transferencia.setContaDestino(contaDestino);
        transferencia.setTipo(form.getTipo());
        transferencia.setValor(NumeroUtil.parseMonetario(form.getValor()));
        transferencia.setDataBase(form.getDataBase());
        transferencia.setDataFimRecorrencia(form.getDataFimRecorrencia());
        transferencia.setTotalParcelas(form.getTotalParcelas());
        transferencia.setObservacao(TextoUtil.paraMaiusculo(form.getObservacao()));
    }

    /**
     * Gera ocorrencia mensal da transferencia quando aplicavel.
     */
    private Optional<OcorrenciaTransferenciaDto> gerarOcorrencia(Transferencia transferencia, YearMonth competencia) {
        YearMonth competenciaBase = YearMonth.from(transferencia.getDataBase());
        YearMonth competenciaFimRecorrencia = transferencia.getDataFimRecorrencia() == null
                ? null
                : YearMonth.from(transferencia.getDataFimRecorrencia());

        Integer parcelaAtual = null;
        Integer totalParcelas = transferencia.getTotalParcelas();

        switch (transferencia.getTipo()) {
            case AVULSO -> {
                if (!competencia.equals(competenciaBase)) {
                    return Optional.empty();
                }
            }
            case RECORRENTE -> {
                if (competencia.isBefore(competenciaBase)) {
                    return Optional.empty();
                }
                if (competenciaFimRecorrencia != null && competencia.isAfter(competenciaFimRecorrencia)) {
                    return Optional.empty();
                }
            }
            case PARCELADO -> {
                if (totalParcelas == null || totalParcelas < 2) {
                    return Optional.empty();
                }

                long diferencaMeses = ChronoUnit.MONTHS.between(competenciaBase.atDay(1), competencia.atDay(1));
                if (diferencaMeses < 0 || diferencaMeses >= totalParcelas) {
                    return Optional.empty();
                }
                parcelaAtual = (int) diferencaMeses + 1;
            }
        }

        LocalDate dataOcorrencia = competencia.atDay(Math.min(transferencia.getDataBase().getDayOfMonth(), competencia.lengthOfMonth()));

        return Optional.of(new OcorrenciaTransferenciaDto(
                transferencia.getId(),
                transferencia.getDescricao(),
                transferencia.getTipo(),
                transferencia.getValor(),
                dataOcorrencia,
                parcelaAtual,
                totalParcelas,
                transferencia.getContaOrigem().getNome(),
                transferencia.getContaDestino().getNome()
        ));
    }
}
