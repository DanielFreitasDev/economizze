package io.nexus.economizze.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de alteracao de senha de usuario.
 */
@Getter
@Setter
public class AlterarSenhaForm {

    @NotBlank(message = "Nova senha e obrigatoria")
    @Size(min = 6, max = 120)
    private String novaSenha;

    @NotBlank(message = "Confirmacao da senha e obrigatoria")
    private String confirmarSenha;
}
