package io.nexus.economizze.cobranca.service;

import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.cartao.service.CartaoService;
import io.nexus.economizze.categoria.model.Categoria;
import io.nexus.economizze.categoria.service.CategoriaService;
import io.nexus.economizze.cobranca.dto.CobrancaPessoaForm;
import io.nexus.economizze.cobranca.dto.OcorrenciaCobrancaPessoaDto;
import io.nexus.economizze.cobranca.model.CobrancaPessoa;
import io.nexus.economizze.cobranca.repository.CobrancaPessoaRepository;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import io.nexus.economizze.pessoa.model.Pessoa;
import io.nexus.economizze.pessoa.service.PessoaService;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.shared.exception.RecursoNaoEncontradoException;
import io.nexus.economizze.shared.util.NumeroUtil;
import io.nexus.economizze.shared.util.TextoUtil;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para cobrancas de pessoas em cartoes emprestados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CobrancaPessoaService {

    private final CobrancaPessoaRepository cobrancaPessoaRepository;
    private final PessoaService pessoaService;
    private final CartaoService cartaoService;
    private final CategoriaService categoriaService;

    /**
     * Lista cobrancas base com opcao de incluir inativas.
     */
    @Transactional(readOnly = true)
    public List<CobrancaPessoa> listar(boolean incluirInativas) {
        return incluirInativas ? cobrancaPessoaRepository.findAllByOrderByDataBaseDescDescricaoAsc()
                : cobrancaPessoaRepository.findAllByAtivoTrueOrderByDataBaseDescDescricaoAsc();
    }

    /**
     * Busca cobranca por id.
     */
    @Transactional(readOnly = true)
    public CobrancaPessoa buscarPorId(Long id) {
        return cobrancaPessoaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cobranca nao encontrada"));
    }

    /**
     * Cria cobranca por pessoa com validacoes de parcelamento/recorrencia.
     */
    @Transactional
    public CobrancaPessoa criar(CobrancaPessoaForm form) {
        validarFormulario(form);

        CobrancaPessoa cobranca = new CobrancaPessoa();
        preencherCampos(cobranca, form);

        CobrancaPessoa salva = cobrancaPessoaRepository.save(cobranca);
        log.info("Cobranca criada para pessoa {} no cartao {}", salva.getPessoa().getNome(), descricaoCartao(salva.getCartao()));
        return salva;
    }

    /**
     * Atualiza cobranca existente.
     */
    @Transactional
    public CobrancaPessoa atualizar(Long id, CobrancaPessoaForm form) {
        validarFormulario(form);

        CobrancaPessoa cobranca = buscarPorId(id);
        preencherCampos(cobranca, form);

        CobrancaPessoa salva = cobrancaPessoaRepository.save(cobranca);
        log.info("Cobranca atualizada para pessoa {}", salva.getPessoa().getNome());
        return salva;
    }

    /**
     * Desativa cobranca base sem remover historico.
     */
    @Transactional
    public void desativar(Long id) {
        CobrancaPessoa cobranca = buscarPorId(id);
        cobranca.setAtivo(Boolean.FALSE);
        cobrancaPessoaRepository.save(cobranca);
        log.warn("Cobranca desativada: {}", cobranca.getDescricao());
    }

    /**
     * Exclui cobranca base definitivamente.
     */
    @Transactional
    public void excluir(Long id) {
        CobrancaPessoa cobranca = buscarPorId(id);
        cobrancaPessoaRepository.delete(cobranca);
        log.warn("Cobranca excluida: {}", cobranca.getDescricao());
    }

    /**
     * Lista ocorrencias da competencia para todas as pessoas/cartoes.
     */
    @Transactional(readOnly = true)
    public List<OcorrenciaCobrancaPessoaDto> listarOcorrenciasDaCompetencia(YearMonth competencia) {
        return listarOcorrenciasDaCompetenciaInterno(null, competencia);
    }

    /**
     * Lista ocorrencias da competencia para uma pessoa especifica.
     */
    @Transactional(readOnly = true)
    public List<OcorrenciaCobrancaPessoaDto> listarOcorrenciasDaCompetenciaPorPessoa(Long pessoaId, YearMonth competencia) {
        return listarOcorrenciasDaCompetenciaInterno(pessoaId, competencia);
    }

    /**
     * Lista todas as ocorrencias ate a competencia para calculo de saldo acumulado.
     */
    @Transactional(readOnly = true)
    public List<OcorrenciaCobrancaPessoaDto> listarOcorrenciasAteCompetenciaPorPessoa(Long pessoaId, YearMonth competenciaLimite) {
        List<CobrancaPessoa> cobrancas = cobrancaPessoaRepository
                .findAllByAtivoTrueAndDataBaseLessThanEqualOrderByDataBaseAscDescricaoAsc(competenciaLimite.atEndOfMonth());

        List<OcorrenciaCobrancaPessoaDto> ocorrencias = new ArrayList<>();

        for (CobrancaPessoa cobranca : cobrancas) {
            if (pessoaId != null && !cobranca.getPessoa().getId().equals(pessoaId)) {
                continue;
            }
            ocorrencias.addAll(gerarOcorrenciasAteCompetencia(cobranca, competenciaLimite));
        }

        ocorrencias.sort(Comparator.comparing(OcorrenciaCobrancaPessoaDto::competencia)
                .thenComparing(OcorrenciaCobrancaPessoaDto::pessoaNome)
                .thenComparing(OcorrenciaCobrancaPessoaDto::descricao));
        return ocorrencias;
    }

    /**
     * Valida regras de negocio por tipo da cobranca.
     */
    private void validarFormulario(CobrancaPessoaForm form) {
        if (form.getTipo() == TipoLancamento.PARCELADO) {
            if (form.getTotalParcelas() == null || form.getTotalParcelas() < 2) {
                throw new RegraDeNegocioException("Cobranca parcelada exige total de parcelas >= 2");
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
     * Copia campos do formulario para entidade persistente.
     */
    private void preencherCampos(CobrancaPessoa cobranca, CobrancaPessoaForm form) {
        Pessoa pessoa = pessoaService.buscarPorId(form.getPessoaId());
        Cartao cartao = cartaoService.buscarPorId(form.getCartaoId());
        Categoria categoria = form.getCategoriaId() == null ? null : categoriaService.buscarPorId(form.getCategoriaId());

        cobranca.setPessoa(pessoa);
        cobranca.setCartao(cartao);
        cobranca.setCategoria(categoria);
        cobranca.setDescricao(TextoUtil.paraMaiusculo(form.getDescricao()));
        cobranca.setTipo(form.getTipo());
        cobranca.setValor(NumeroUtil.parseMonetario(form.getValor()));
        cobranca.setDataBase(form.getDataBase());
        cobranca.setDataFimRecorrencia(form.getDataFimRecorrencia());
        cobranca.setTotalParcelas(form.getTotalParcelas());
        cobranca.setObservacao(TextoUtil.paraMaiusculo(form.getObservacao()));

        // Ajusta competencia inicial da fatura conforme escolha manual ou regra automatica do cartao.
        YearMonth competenciaInicial = form.getCompetenciaFaturaInicial() != null
                ? YearMonth.from(form.getCompetenciaFaturaInicial())
                : cartaoService.calcularCompetenciaDaCompra(cartao, form.getDataBase());
        cobranca.setCompetenciaFaturaInicial(competenciaInicial.atDay(1));
    }

    /**
     * Lista ocorrencias de uma competencia filtrando opcionalmente por pessoa.
     */
    private List<OcorrenciaCobrancaPessoaDto> listarOcorrenciasDaCompetenciaInterno(Long pessoaId, YearMonth competencia) {
        List<CobrancaPessoa> cobrancas = cobrancaPessoaRepository
                .findAllByAtivoTrueAndDataBaseLessThanEqualOrderByDataBaseAscDescricaoAsc(competencia.atEndOfMonth());

        List<OcorrenciaCobrancaPessoaDto> ocorrencias = new ArrayList<>();
        for (CobrancaPessoa cobranca : cobrancas) {
            if (pessoaId != null && !cobranca.getPessoa().getId().equals(pessoaId)) {
                continue;
            }
            OcorrenciaCobrancaPessoaDto ocorrencia = gerarOcorrencia(cobranca, competencia);
            if (ocorrencia != null) {
                ocorrencias.add(ocorrencia);
            }
        }

        ocorrencias.sort(Comparator.comparing(OcorrenciaCobrancaPessoaDto::pessoaNome)
                .thenComparing(OcorrenciaCobrancaPessoaDto::cartaoDescricao)
                .thenComparing(OcorrenciaCobrancaPessoaDto::descricao));
        return ocorrencias;
    }

    /**
     * Gera ocorrencia da cobranca na competencia alvo.
     */
    private OcorrenciaCobrancaPessoaDto gerarOcorrencia(CobrancaPessoa cobranca, YearMonth competencia) {
        YearMonth competenciaBase = YearMonth.from(cobranca.getCompetenciaFaturaInicial());
        YearMonth competenciaFimRecorrencia = cobranca.getDataFimRecorrencia() == null
                ? null
                : YearMonth.from(cobranca.getDataFimRecorrencia());

        Integer parcelaAtual = null;
        Integer totalParcelas = cobranca.getTotalParcelas();

        switch (cobranca.getTipo()) {
            case AVULSO -> {
                if (!competencia.equals(competenciaBase)) {
                    return null;
                }
            }
            case RECORRENTE -> {
                if (competencia.isBefore(competenciaBase)) {
                    return null;
                }
                if (competenciaFimRecorrencia != null && competencia.isAfter(competenciaFimRecorrencia)) {
                    return null;
                }
            }
            case PARCELADO -> {
                if (totalParcelas == null || totalParcelas < 2) {
                    return null;
                }
                int indice = (int) competenciaBase.until(competencia, java.time.temporal.ChronoUnit.MONTHS);
                if (indice < 0 || indice >= totalParcelas) {
                    return null;
                }
                parcelaAtual = indice + 1;
            }
        }

        return new OcorrenciaCobrancaPessoaDto(
                cobranca.getId(),
                cobranca.getPessoa().getId(),
                cobranca.getPessoa().getNome(),
                cobranca.getCartao().getId(),
                descricaoCartao(cobranca.getCartao()),
                cobranca.getDescricao(),
                cobranca.getTipo(),
                cobranca.getValor(),
                competencia.atDay(1),
                parcelaAtual,
                totalParcelas
        );
    }

    /**
     * Gera ocorrencias da cobranca de sua competencia inicial ate o limite informado.
     */
    private List<OcorrenciaCobrancaPessoaDto> gerarOcorrenciasAteCompetencia(CobrancaPessoa cobranca, YearMonth competenciaLimite) {
        List<OcorrenciaCobrancaPessoaDto> ocorrencias = new ArrayList<>();
        YearMonth competenciaAtual = YearMonth.from(cobranca.getCompetenciaFaturaInicial());
        YearMonth competenciaFimRecorrencia = cobranca.getDataFimRecorrencia() == null
                ? null
                : YearMonth.from(cobranca.getDataFimRecorrencia());

        switch (cobranca.getTipo()) {
            case AVULSO -> {
                if (!competenciaAtual.isAfter(competenciaLimite)) {
                    ocorrencias.add(gerarOcorrencia(cobranca, competenciaAtual));
                }
            }
            case RECORRENTE -> {
                while (!competenciaAtual.isAfter(competenciaLimite)
                        && (competenciaFimRecorrencia == null || !competenciaAtual.isAfter(competenciaFimRecorrencia))) {
                    ocorrencias.add(gerarOcorrencia(cobranca, competenciaAtual));
                    competenciaAtual = competenciaAtual.plusMonths(1);
                }
            }
            case PARCELADO -> {
                int totalParcelas = cobranca.getTotalParcelas() == null ? 0 : cobranca.getTotalParcelas();
                for (int indice = 0; indice < totalParcelas; indice++) {
                    YearMonth competenciaParcela = YearMonth.from(cobranca.getCompetenciaFaturaInicial()).plusMonths(indice);
                    if (competenciaParcela.isAfter(competenciaLimite)) {
                        break;
                    }
                    ocorrencias.add(gerarOcorrencia(cobranca, competenciaParcela));
                }
            }
        }

        ocorrencias.removeIf(java.util.Objects::isNull);
        return ocorrencias;
    }

    /**
     * Monta descricao amigavel de cartao para listagens e relatorios.
     */
    private String descricaoCartao(Cartao cartao) {
        String numero = cartao.getNumeroCartao();
        String ultimosQuatro = numero.length() >= 4 ? numero.substring(numero.length() - 4) : "****";
        return cartao.getBanco() + " **** " + ultimosQuatro;
    }
}
