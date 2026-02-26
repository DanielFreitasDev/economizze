package io.nexus.economizze.lancamento.service;

import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.cartao.service.CartaoService;
import io.nexus.economizze.categoria.model.Categoria;
import io.nexus.economizze.categoria.service.CategoriaService;
import io.nexus.economizze.conta.model.Conta;
import io.nexus.economizze.conta.service.ContaService;
import io.nexus.economizze.lancamento.dto.LancamentoForm;
import io.nexus.economizze.lancamento.dto.OcorrenciaLancamentoDto;
import io.nexus.economizze.lancamento.model.Lancamento;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import io.nexus.economizze.lancamento.repository.LancamentoRepository;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.shared.exception.RecursoNaoEncontradoException;
import io.nexus.economizze.shared.util.NumeroUtil;
import io.nexus.economizze.shared.util.TextoUtil;
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
 * Regras de negocio para lancamentos de receitas e despesas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LancamentoService {

    private final LancamentoRepository lancamentoRepository;
    private final CategoriaService categoriaService;
    private final ContaService contaService;
    private final CartaoService cartaoService;

    /**
     * Lista lancamentos base com opcao de incluir inativos.
     */
    @Transactional(readOnly = true)
    public List<Lancamento> listar(boolean incluirInativos) {
        return incluirInativos ? lancamentoRepository.findAllByOrderByDataBaseDescNomeAsc() : lancamentoRepository.findAllByAtivoTrueOrderByDataBaseDescNomeAsc();
    }

    /**
     * Busca lancamento por id com validacao.
     */
    @Transactional(readOnly = true)
    public Lancamento buscarPorId(Long id) {
        return lancamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lancamento nao encontrado"));
    }

    /**
     * Cria lancamento base com validacoes por tipo e origem.
     */
    @Transactional
    public Lancamento criar(LancamentoForm form) {
        validarRegrasFormulario(form);

        Lancamento lancamento = new Lancamento();
        preencherCampos(lancamento, form);

        Lancamento salvo = lancamentoRepository.save(lancamento);
        log.info("Lancamento criado: {} ({})", salvo.getNome(), salvo.getTipo());
        return salvo;
    }

    /**
     * Atualiza lancamento existente com revalidacao de regras.
     */
    @Transactional
    public Lancamento atualizar(Long id, LancamentoForm form) {
        validarRegrasFormulario(form);

        Lancamento lancamento = buscarPorId(id);
        preencherCampos(lancamento, form);

        Lancamento salvo = lancamentoRepository.save(lancamento);
        log.info("Lancamento atualizado: {} ({})", salvo.getNome(), salvo.getTipo());
        return salvo;
    }

    /**
     * Desativa lancamento para parar a projeção futura sem apagar historico.
     */
    @Transactional
    public void desativar(Long id) {
        Lancamento lancamento = buscarPorId(id);
        lancamento.setAtivo(Boolean.FALSE);
        lancamentoRepository.save(lancamento);
        log.warn("Lancamento desativado: {}", lancamento.getNome());
    }

    /**
     * Exclui lancamento definitivamente.
     */
    @Transactional
    public void excluir(Long id) {
        Lancamento lancamento = buscarPorId(id);
        lancamentoRepository.delete(lancamento);
        log.warn("Lancamento excluido: {}", lancamento.getNome());
    }

    /**
     * Projeta ocorrencias de lancamentos para uma competencia mensal.
     */
    @Transactional(readOnly = true)
    public List<OcorrenciaLancamentoDto> listarOcorrenciasDaCompetencia(YearMonth competencia) {
        LocalDate fimCompetencia = competencia.atEndOfMonth();

        List<Lancamento> lancamentos = lancamentoRepository.findAllByAtivoTrueAndDataBaseLessThanEqualOrderByDataBaseAscNomeAsc(fimCompetencia);
        List<OcorrenciaLancamentoDto> ocorrencias = new ArrayList<>();

        for (Lancamento lancamento : lancamentos) {
            gerarOcorrencia(lancamento, competencia).ifPresent(ocorrencias::add);
        }

        ocorrencias.sort(Comparator.comparing(OcorrenciaLancamentoDto::dataOcorrencia).thenComparing(OcorrenciaLancamentoDto::nome));
        return ocorrencias;
    }

    /**
     * Valida regras especificas de tipo, parcelas e origem do lancamento.
     */
    private void validarRegrasFormulario(LancamentoForm form) {
        boolean temConta = form.getContaId() != null;
        boolean temCartao = form.getCartaoId() != null;

        if (temConta == temCartao) {
            throw new RegraDeNegocioException("Informe somente conta ou cartao");
        }

        if (form.getTipo() == TipoLancamento.PARCELADO) {
            if (form.getTotalParcelas() == null || form.getTotalParcelas() < 2) {
                throw new RegraDeNegocioException("Lancamento parcelado exige total de parcelas >= 2");
            }
        }

        if (form.getTipo() == TipoLancamento.RECORRENTE && form.getDataFimRecorrencia() != null
                && form.getDataFimRecorrencia().isBefore(form.getDataBase())) {
            throw new RegraDeNegocioException("Data fim da recorrencia nao pode ser menor que a data base");
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
     * Copia dados do formulario para a entidade de dominio.
     */
    private void preencherCampos(Lancamento lancamento, LancamentoForm form) {
        Categoria categoria = categoriaService.buscarPorId(form.getCategoriaId());
        Conta conta = form.getContaId() == null ? null : contaService.buscarPorId(form.getContaId());
        Cartao cartao = form.getCartaoId() == null ? null : cartaoService.buscarPorId(form.getCartaoId());

        lancamento.setNome(TextoUtil.paraMaiusculo(form.getNome()));
        lancamento.setNatureza(form.getNatureza());
        lancamento.setTipo(form.getTipo());
        lancamento.setValor(NumeroUtil.parseMonetario(form.getValor()));
        lancamento.setCategoria(categoria);
        lancamento.setConta(conta);
        lancamento.setCartao(cartao);
        lancamento.setDataBase(form.getDataBase());
        lancamento.setDataFimRecorrencia(form.getDataFimRecorrencia());
        lancamento.setTotalParcelas(form.getTotalParcelas());
        lancamento.setObservacao(TextoUtil.paraMaiusculo(form.getObservacao()));

        // Para cartao, define competencia inicial automaticamente quando nao informada.
        if (cartao != null) {
            YearMonth competencia = form.getCompetenciaFaturaInicial() != null
                    ? YearMonth.from(form.getCompetenciaFaturaInicial())
                    : cartaoService.calcularCompetenciaDaCompra(cartao, form.getDataBase());
            lancamento.setCompetenciaFaturaInicial(competencia.atDay(1));
        } else {
            lancamento.setCompetenciaFaturaInicial(null);
        }
    }

    /**
     * Gera uma ocorrencia mensal para o lancamento quando a competencia se aplica.
     */
    private Optional<OcorrenciaLancamentoDto> gerarOcorrencia(Lancamento lancamento, YearMonth competencia) {
        YearMonth competenciaBase = obterCompetenciaBase(lancamento);
        YearMonth competenciaFimRecorrencia = lancamento.getDataFimRecorrencia() == null
                ? null
                : YearMonth.from(lancamento.getDataFimRecorrencia());

        Integer parcelaAtual = null;
        Integer totalParcelas = lancamento.getTotalParcelas();

        switch (lancamento.getTipo()) {
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

        LocalDate dataOcorrencia = competencia.atDay(Math.min(lancamento.getDataBase().getDayOfMonth(), competencia.lengthOfMonth()));
        String origem = montarOrigem(lancamento);
        String cartaoDescricao = lancamento.getCartao() == null ? null : descricaoCartao(lancamento.getCartao());

        return Optional.of(new OcorrenciaLancamentoDto(
                lancamento.getId(),
                lancamento.getNome(),
                lancamento.getNatureza(),
                lancamento.getTipo(),
                lancamento.getValor(),
                dataOcorrencia,
                parcelaAtual,
                totalParcelas,
                lancamento.getCategoria().getNome(),
                origem,
                lancamento.getConta() == null ? null : lancamento.getConta().getId(),
                lancamento.getCartao() == null ? null : lancamento.getCartao().getId(),
                cartaoDescricao
        ));
    }

    /**
     * Retorna competencia base considerando regra de cartao x conta.
     */
    private YearMonth obterCompetenciaBase(Lancamento lancamento) {
        if (lancamento.getCartao() != null && lancamento.getCompetenciaFaturaInicial() != null) {
            return YearMonth.from(lancamento.getCompetenciaFaturaInicial());
        }
        return YearMonth.from(lancamento.getDataBase());
    }

    /**
     * Monta descricao amigavel da origem financeira.
     */
    private String montarOrigem(Lancamento lancamento) {
        if (lancamento.getConta() != null) {
            return "CONTA - " + lancamento.getConta().getNome();
        }

        if (lancamento.getCartao() != null) {
            return "CARTAO - " + descricaoCartao(lancamento.getCartao());
        }

        return "ORIGEM NAO INFORMADA";
    }

    /**
     * Monta descricao de cartao com banco e quatro ultimos digitos.
     */
    private String descricaoCartao(Cartao cartao) {
        String numero = cartao.getNumeroCartao();
        String ultimosQuatro = numero.length() >= 4 ? numero.substring(numero.length() - 4) : "****";
        return cartao.getBanco() + " **** " + ultimosQuatro;
    }
}
