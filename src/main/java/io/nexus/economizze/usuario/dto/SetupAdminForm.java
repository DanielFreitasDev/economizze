package io.nexus.economizze.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de criacao do primeiro administrador.
 */
@Getter
@Setter
public class SetupAdminForm {

    @NotBlank(message = "Nome completo e obrigatorio")
    @Size(max = 150)
    private String nomeCompleto;

    @NotBlank(message = "Usuario e obrigatorio")
    @Size(max = 80)
    private String username;

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 6, max = 120)
    private String senha;

    @NotBlank(message = "Confirmacao de senha e obrigatoria")
    private String confirmarSenha;
}
