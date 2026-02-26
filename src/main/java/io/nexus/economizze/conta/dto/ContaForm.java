package io.nexus.economizze.conta.dto;

import io.nexus.economizze.conta.model.TipoConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de cadastro e edicao de conta.
 */
@Getter
@Setter
public class ContaForm {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 120)
    private String nome;

    @Size(max = 120)
    private String banco;

    @NotNull(message = "Tipo da conta e obrigatorio")
    private TipoConta tipoConta;

    @NotBlank(message = "Saldo inicial e obrigatorio")
    private String saldoInicial;
}
