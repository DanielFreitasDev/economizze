package io.nexus.economizze.cobranca.dto;

import io.nexus.economizze.cobranca.model.TipoMovimentoPessoa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Formulario de cadastro e edicao de pagamentos/encargos por pessoa.
 */
@Getter
@Setter
public class PagamentoPessoaForm {

    @NotNull(message = "Pessoa e obrigatoria")
    private Long pessoaId;

    @NotNull(message = "Cartao e obrigatorio")
    private Long cartaoId;

    @NotBlank(message = "Descricao e obrigatoria")
    @Size(max = 200)
    private String descricao;

    @NotNull(message = "Tipo do movimento e obrigatorio")
    private TipoMovimentoPessoa tipo;

    @NotBlank(message = "Valor e obrigatorio")
    private String valor;

    @NotNull(message = "Data e obrigatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPagamento;
}
