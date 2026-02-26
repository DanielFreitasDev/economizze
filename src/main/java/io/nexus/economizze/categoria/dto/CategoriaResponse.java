package io.nexus.economizze.categoria.dto;

/**
 * DTO de resposta de categoria para API.
 */
public record CategoriaResponse(
        Long id,
        String nome,
        String descricao,
        Boolean ativo
) {
}
