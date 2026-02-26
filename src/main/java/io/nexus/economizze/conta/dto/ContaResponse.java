package io.nexus.economizze.conta.dto;

import io.nexus.economizze.conta.model.TipoConta;
import java.math.BigDecimal;

/**
 * DTO de resposta de conta para API.
 */
public record ContaResponse(
        Long id,
        String nome,
        String banco,
        TipoConta tipoConta,
        BigDecimal saldoInicial,
        Boolean ativo
) {
}
