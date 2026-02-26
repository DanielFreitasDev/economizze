package io.nexus.economizze.relatorio.dto;

import java.math.BigDecimal;

/**
 * DTO de resumo geral de saldo em aberto por pessoa.
 */
public record ResumoPessoaGeralDto(
        Long pessoaId,
        String pessoaNome,
        String cpf,
        BigDecimal saldoAberto
) {
}
