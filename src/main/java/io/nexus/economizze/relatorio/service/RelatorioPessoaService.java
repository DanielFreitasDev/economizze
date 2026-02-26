package io.nexus.economizze.relatorio.service;

import io.nexus.economizze.cobranca.dto.OcorrenciaCobrancaPessoaDto;
import io.nexus.economizze.cobranca.dto.RelatorioPessoaDto;
import io.nexus.economizze.cobranca.dto.ResumoCartaoPessoaDto;
import io.nexus.economizze.cobranca.model.PagamentoPessoa;
import io.nexus.economizze.cobranca.service.CobrancaPessoaService;
import io.nexus.economizze.cobranca.service.PagamentoPessoaService;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import io.nexus.economizze.pessoa.model.Pessoa;
import io.nexus.economizze.pessoa.service.PessoaService;
import io.nexus.economizze.relatorio.dto.ResumoPessoaGeralDto;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service que consolida dados analiticos de divida por pessoa/cartao.
 */
@Service
@RequiredArgsConstructor
public class RelatorioPessoaService {

    private final PessoaService pessoaService;
    private final CobrancaPessoaService cobrancaPessoaService;
    private final PagamentoPessoaService pagamentoPessoaService;

    /**
     * Gera relatorio consolidado para uma pessoa em determinada competencia.
     */
    @Transactional(readOnly = true)
    public RelatorioPessoaDto gerarRelatorio(Long pessoaId, YearMonth competencia) {
        Pessoa pessoa = pessoaService.buscarPorId(pessoaId);

        List<OcorrenciaCobrancaPessoaDto> ocorrenciasAteCompetencia = cobrancaPessoaService
                .listarOcorrenciasAteCompetenciaPorPessoa(pessoaId, competencia);

        List<OcorrenciaCobrancaPessoaDto> ocorrenciasCompetencia = ocorrenciasAteCompetencia.stream()
                .filter(ocorrencia -> YearMonth.from(ocorrencia.competencia()).equals(competencia))
                .sorted(Comparator.comparing(OcorrenciaCobrancaPessoaDto::cartaoDescricao)
                        .thenComparing(OcorrenciaCobrancaPessoaDto::descricao))
                .toList();

        List<PagamentoPessoa> movimentosAteCompetencia = pagamentoPessoaService
                .listarMovimentosAteCompetenciaPorPessoa(pessoaId, competencia);

        Set<Long> cartoesIds = new LinkedHashSet<>();
        ocorrenciasAteCompetencia.forEach(ocorrencia -> cartoesIds.add(ocorrencia.cartaoId()));
        movimentosAteCompetencia.forEach(movimento -> cartoesIds.add(movimento.getCartao().getId()));

        List<ResumoCartaoPessoaDto> resumos = new ArrayList<>();

        for (Long cartaoId : cartoesIds) {
            List<OcorrenciaCobrancaPessoaDto> cobrancasCartaoCompetencia = ocorrenciasCompetencia.stream()
                    .filter(ocorrencia -> ocorrencia.cartaoId().equals(cartaoId))
                    .toList();

            List<OcorrenciaCobrancaPessoaDto> cobrancasCartaoAteCompetencia = ocorrenciasAteCompetencia.stream()
                    .filter(ocorrencia -> ocorrencia.cartaoId().equals(cartaoId))
                    .toList();

            List<PagamentoPessoa> movimentosCartaoAteCompetencia = movimentosAteCompetencia.stream()
                    .filter(movimento -> movimento.getCartao().getId().equals(cartaoId))
                    .toList();

            List<PagamentoPessoa> movimentosCartaoCompetencia = movimentosCartaoAteCompetencia.stream()
                    .filter(movimento -> YearMonth.from(movimento.getDataPagamento()).equals(competencia))
                    .toList();

            BigDecimal totalAvulso = somarPorTipo(cobrancasCartaoCompetencia, TipoLancamento.AVULSO);
            BigDecimal totalRecorrente = somarPorTipo(cobrancasCartaoCompetencia, TipoLancamento.RECORRENTE);
            BigDecimal totalParcelado = somarPorTipo(cobrancasCartaoCompetencia, TipoLancamento.PARCELADO);
            BigDecimal totalEncargos = somarMovimentosDebito(movimentosCartaoCompetencia);
            BigDecimal totalPagamentos = somarMovimentosCredito(movimentosCartaoCompetencia);
            BigDecimal saldoAberto = calcularSaldoAcumulado(cobrancasCartaoAteCompetencia, movimentosCartaoAteCompetencia, competencia);

            String cartaoDescricao = cobrancasCartaoAteCompetencia.stream()
                    .findFirst()
                    .map(OcorrenciaCobrancaPessoaDto::cartaoDescricao)
                    .orElseGet(() -> descricaoCartao(movimentosCartaoAteCompetencia));

            resumos.add(new ResumoCartaoPessoaDto(
                    cartaoId,
                    cartaoDescricao,
                    totalAvulso,
                    totalRecorrente,
                    totalParcelado,
                    totalEncargos,
                    totalPagamentos,
                    saldoAberto
            ));
        }

        resumos.sort(Comparator.comparing(ResumoCartaoPessoaDto::cartaoDescricao));

        BigDecimal totalGeralAberto = resumos.stream()
                .map(ResumoCartaoPessoaDto::saldoAberto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioPessoaDto(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                competencia,
                resumos,
                ocorrenciasCompetencia,
                totalGeralAberto
        );
    }

    /**
     * Retorna resumo geral de saldo em aberto para todas as pessoas ativas.
     */
    @Transactional(readOnly = true)
    public List<ResumoPessoaGeralDto> listarResumoGeral(YearMonth competencia) {
        return pessoaService.listar(false).stream()
                .map(pessoa -> {
                    RelatorioPessoaDto relatorio = gerarRelatorio(pessoa.getId(), competencia);
                    return new ResumoPessoaGeralDto(
                            pessoa.getId(),
                            pessoa.getNome(),
                            pessoa.getCpf(),
                            relatorio.totalGeralAberto()
                    );
                })
                .sorted(Comparator.comparing(ResumoPessoaGeralDto::pessoaNome))
                .toList();
    }

    /**
     * Soma valores por tipo de cobranca no mes.
     */
    private BigDecimal somarPorTipo(List<OcorrenciaCobrancaPessoaDto> cobrancas, TipoLancamento tipo) {
        return cobrancas.stream()
                .filter(cobranca -> cobranca.tipo() == tipo)
                .map(OcorrenciaCobrancaPessoaDto::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Soma movimentos que reduzem a divida (pagamentos e creditos).
     */
    private BigDecimal somarMovimentosCredito(List<PagamentoPessoa> movimentos) {
        return movimentos.stream()
                .filter(movimento -> pagamentoPessoaService.isTipoCredito(movimento.getTipo()))
                .map(PagamentoPessoa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Soma movimentos que aumentam a divida (encargos e ajustes debito).
     */
    private BigDecimal somarMovimentosDebito(List<PagamentoPessoa> movimentos) {
        return movimentos.stream()
                .filter(movimento -> pagamentoPessoaService.isTipoDebito(movimento.getTipo()))
                .map(PagamentoPessoa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula saldo acumulado por cartao ate a competencia com reaproveitamento de pagamentos excedentes.
     */
    private BigDecimal calcularSaldoAcumulado(
            List<OcorrenciaCobrancaPessoaDto> cobrancas,
            List<PagamentoPessoa> movimentos,
            YearMonth competenciaLimite
    ) {
        Map<YearMonth, BigDecimal> debitosPorMes = new HashMap<>();
        Map<YearMonth, BigDecimal> creditosPorMes = new HashMap<>();

        for (OcorrenciaCobrancaPessoaDto cobranca : cobrancas) {
            YearMonth competencia = YearMonth.from(cobranca.competencia());
            debitosPorMes.merge(competencia, cobranca.valor(), BigDecimal::add);
        }

        for (PagamentoPessoa movimento : movimentos) {
            YearMonth competencia = YearMonth.from(movimento.getDataPagamento());
            if (pagamentoPessoaService.isTipoCredito(movimento.getTipo())) {
                creditosPorMes.merge(competencia, movimento.getValor(), BigDecimal::add);
            } else if (pagamentoPessoaService.isTipoDebito(movimento.getTipo())) {
                debitosPorMes.merge(competencia, movimento.getValor(), BigDecimal::add);
            }
        }

        YearMonth primeiraCompetencia = descobrirPrimeiraCompetencia(cobrancas, movimentos, competenciaLimite);
        BigDecimal saldo = BigDecimal.ZERO;
        YearMonth cursor = primeiraCompetencia;

        while (!cursor.isAfter(competenciaLimite)) {
            BigDecimal debitos = debitosPorMes.getOrDefault(cursor, BigDecimal.ZERO);
            BigDecimal creditos = creditosPorMes.getOrDefault(cursor, BigDecimal.ZERO);

            // Saldo pode ficar negativo para representar credito que sera aproveitado nos meses seguintes.
            saldo = saldo.add(debitos).subtract(creditos);
            cursor = cursor.plusMonths(1);
        }

        return saldo.max(BigDecimal.ZERO);
    }

    /**
     * Descobre a menor competencia existente para iniciar o calculo acumulado.
     */
    private YearMonth descobrirPrimeiraCompetencia(
            List<OcorrenciaCobrancaPessoaDto> cobrancas,
            List<PagamentoPessoa> movimentos,
            YearMonth competenciaPadrao
    ) {
        YearMonth menor = competenciaPadrao;

        for (OcorrenciaCobrancaPessoaDto cobranca : cobrancas) {
            YearMonth competencia = YearMonth.from(cobranca.competencia());
            if (competencia.isBefore(menor)) {
                menor = competencia;
            }
        }

        for (PagamentoPessoa movimento : movimentos) {
            YearMonth competencia = YearMonth.from(movimento.getDataPagamento());
            if (competencia.isBefore(menor)) {
                menor = competencia;
            }
        }

        return menor;
    }

    /**
     * Monta descricao do cartao com banco e quatro ultimos digitos quando houver so movimentos.
     */
    private String descricaoCartao(List<PagamentoPessoa> movimentosCartao) {
        return movimentosCartao.stream()
                .findFirst()
                .map(movimento -> {
                    String numero = movimento.getCartao().getNumeroCartao();
                    String ultimosQuatro = numero.length() >= 4 ? numero.substring(numero.length() - 4) : "****";
                    return movimento.getCartao().getBanco() + " **** " + ultimosQuatro;
                })
                .orElse("CARTAO NAO IDENTIFICADO");
    }
}
