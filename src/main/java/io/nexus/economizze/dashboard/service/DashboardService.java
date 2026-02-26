package io.nexus.economizze.dashboard.service;

import io.nexus.economizze.cobranca.dto.OcorrenciaCobrancaPessoaDto;
import io.nexus.economizze.cobranca.service.CobrancaPessoaService;
import io.nexus.economizze.dashboard.controller.DashboardResumoDto;
import io.nexus.economizze.lancamento.dto.OcorrenciaLancamentoDto;
import io.nexus.economizze.lancamento.model.NaturezaLancamento;
import io.nexus.economizze.lancamento.service.LancamentoService;
import io.nexus.economizze.transferencia.dto.OcorrenciaTransferenciaDto;
import io.nexus.economizze.transferencia.service.TransferenciaService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de consolidacao dos dados exibidos no dashboard mensal.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LancamentoService lancamentoService;
    private final TransferenciaService transferenciaService;
    private final CobrancaPessoaService cobrancaPessoaService;

    /**
     * Lista ocorrencias de lancamentos para a competencia selecionada.
     */
    @Transactional(readOnly = true)
    public List<OcorrenciaLancamentoDto> listarLancamentosCompetencia(YearMonth competencia) {
        return lancamentoService.listarOcorrenciasDaCompetencia(competencia);
    }

    /**
     * Lista ocorrencias de transferencias para a competencia selecionada.
     */
    @Transactional(readOnly = true)
    public List<OcorrenciaTransferenciaDto> listarTransferenciasCompetencia(YearMonth competencia) {
        return transferenciaService.listarOcorrenciasDaCompetencia(competencia);
    }

    /**
     * Lista ocorrencias de cobrancas de terceiros por cartao na competencia.
     */
    @Transactional(readOnly = true)
    public List<OcorrenciaCobrancaPessoaDto> listarCobrancasCompetencia(YearMonth competencia) {
        return cobrancaPessoaService.listarOcorrenciasDaCompetencia(competencia);
    }

    /**
     * Calcula indicadores financeiros principais para o dashboard.
     */
    @Transactional(readOnly = true)
    public DashboardResumoDto calcularResumo(YearMonth competencia) {
        List<OcorrenciaLancamentoDto> lancamentos = listarLancamentosCompetencia(competencia);
        List<OcorrenciaTransferenciaDto> transferencias = listarTransferenciasCompetencia(competencia);
        List<OcorrenciaCobrancaPessoaDto> cobrancas = listarCobrancasCompetencia(competencia);

        BigDecimal totalReceitas = lancamentos.stream()
                .filter(lancamento -> lancamento.natureza() == NaturezaLancamento.RECEITA)
                .map(OcorrenciaLancamentoDto::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = lancamentos.stream()
                .filter(lancamento -> lancamento.natureza() == NaturezaLancamento.DESPESA)
                .map(OcorrenciaLancamentoDto::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTransferencias = transferencias.stream()
                .map(OcorrenciaTransferenciaDto::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCobrancasTerceiros = cobrancas.stream()
                .map(OcorrenciaCobrancaPessoaDto::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Minha parcela da fatura e estimada pelo total de despesas de cartao proprias do mes.
        BigDecimal minhaParcelaFatura = lancamentos.stream()
                .filter(lancamento -> lancamento.cartaoId() != null)
                .filter(lancamento -> lancamento.natureza() == NaturezaLancamento.DESPESA)
                .map(OcorrenciaLancamentoDto::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mapa mantido para futura exibicao analitica por cartao no dashboard.
        Map<Long, BigDecimal> cobrancasPorCartao = new HashMap<>();
        for (OcorrenciaCobrancaPessoaDto cobranca : cobrancas) {
            cobrancasPorCartao.merge(cobranca.cartaoId(), cobranca.valor(), BigDecimal::add);
        }

        return new DashboardResumoDto(
                totalReceitas,
                totalDespesas,
                totalTransferencias,
                totalCobrancasTerceiros,
                minhaParcelaFatura
        );
    }
}
