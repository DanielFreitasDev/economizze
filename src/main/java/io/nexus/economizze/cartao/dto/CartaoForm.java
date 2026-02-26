package io.nexus.economizze.cartao.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de cadastro e edicao de cartao.
 */
@Getter
@Setter
public class CartaoForm {

    @NotBlank(message = "Numero do cartao e obrigatorio")
    @Pattern(regexp = "^[0-9\\s]{16,19}$", message = "Numero do cartao invalido")
    private String numeroCartao;

    @NotBlank(message = "Bandeira e obrigatoria")
    @Size(max = 60)
    private String bandeira;

    @NotBlank(message = "Banco e obrigatorio")
    @Size(max = 120)
    private String banco;

    @NotNull(message = "Dia de fechamento e obrigatorio")
    @Min(value = 1)
    @Max(value = 31)
    private Integer diaFechamento;

    @NotNull(message = "Dia de vencimento e obrigatorio")
    @Min(value = 1)
    @Max(value = 31)
    private Integer diaVencimento;
}
