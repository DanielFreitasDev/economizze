package io.nexus.economizze.categoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de cadastro e edicao de categoria.
 */
@Getter
@Setter
public class CategoriaForm {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 120)
    private String nome;

    @Size(max = 220)
    private String descricao;
}
