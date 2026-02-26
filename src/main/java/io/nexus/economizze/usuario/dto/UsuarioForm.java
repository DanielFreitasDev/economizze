package io.nexus.economizze.usuario.dto;

import io.nexus.economizze.usuario.model.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de cadastro e edicao de usuario.
 */
@Getter
@Setter
public class UsuarioForm {

    @NotBlank(message = "Nome completo e obrigatorio")
    @Size(max = 150)
    private String nomeCompleto;

    @NotBlank(message = "Usuario e obrigatorio")
    @Size(max = 80)
    private String username;

    @NotNull(message = "Perfil e obrigatorio")
    private PerfilUsuario perfil;

    @Size(min = 6, max = 120)
    private String senha;
}
