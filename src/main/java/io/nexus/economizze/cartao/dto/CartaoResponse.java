package io.nexus.economizze.cartao.dto;

/**
 * DTO de resposta de cartao para API.
 */
public record CartaoResponse(
        Long id,
        String numeroCartao,
        String bandeira,
        String banco,
        Integer diaFechamento,
        Integer diaVencimento,
        Boolean ativo
) {
}
