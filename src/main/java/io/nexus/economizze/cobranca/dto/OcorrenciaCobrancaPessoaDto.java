package io.nexus.economizze.cobranca.dto;

import io.nexus.economizze.lancamento.model.TipoLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de ocorrencia mensal de cobranca por pessoa em cartao.
 */
public record OcorrenciaCobrancaPessoaDto(
        Long cobrancaId,
        Long pessoaId,
        String pessoaNome,
        Long cartaoId,
        String cartaoDescricao,
        String descricao,
        TipoLancamento tipo,
        BigDecimal valor,
        LocalDate competencia,
        Integer parcelaAtual,
        Integer totalParcelas
) {
}
