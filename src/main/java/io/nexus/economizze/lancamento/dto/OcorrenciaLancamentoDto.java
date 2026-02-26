package io.nexus.economizze.lancamento.dto;

import io.nexus.economizze.lancamento.model.NaturezaLancamento;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de ocorrencia mensal derivada de um lancamento base.
 */
public record OcorrenciaLancamentoDto(
        Long lancamentoId,
        String nome,
        NaturezaLancamento natureza,
        TipoLancamento tipo,
        BigDecimal valor,
        LocalDate dataOcorrencia,
        Integer parcelaAtual,
        Integer totalParcelas,
        String categoria,
        String origem,
        Long contaId,
        Long cartaoId,
        String cartaoDescricao
) {
}
