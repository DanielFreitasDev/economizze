package io.nexus.economizze.cobranca.dto;

import java.math.BigDecimal;

/**
 * DTO de totalizacao por cartao para relatorio de pessoa.
 */
public record ResumoCartaoPessoaDto(
        Long cartaoId,
        String cartaoDescricao,
        BigDecimal totalAvulso,
        BigDecimal totalRecorrente,
        BigDecimal totalParcelado,
        BigDecimal totalEncargos,
        BigDecimal totalPagamentos,
        BigDecimal saldoAberto
) {
}
