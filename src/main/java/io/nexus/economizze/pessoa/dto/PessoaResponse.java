package io.nexus.economizze.pessoa.dto;

/**
 * DTO de resposta de pessoa para API.
 */
public record PessoaResponse(
        Long id,
        String nome,
        String cpf,
        String telefoneCelular,
        String whatsapp,
        String email,
        Boolean ativo
) {
}
