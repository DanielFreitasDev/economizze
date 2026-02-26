package io.nexus.economizze.cobranca.dto;

import io.nexus.economizze.lancamento.model.TipoLancamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Formulario de cadastro e edicao de cobrancas por pessoa.
 */
@Getter
@Setter
public class CobrancaPessoaForm {

    @NotNull(message = "Pessoa e obrigatoria")
    private Long pessoaId;

    @NotNull(message = "Cartao e obrigatorio")
    private Long cartaoId;

    private Long categoriaId;

    @NotBlank(message = "Descricao e obrigatoria")
    @Size(max = 200)
    private String descricao;

    @NotNull(message = "Tipo e obrigatorio")
    private TipoLancamento tipo;

    @NotBlank(message = "Valor e obrigatorio")
    private String valor;

    @NotNull(message = "Data da compra e obrigatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataBase;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimRecorrencia;

    private Integer totalParcelas;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate competenciaFaturaInicial;

    @Size(max = 320)
    private String observacao;
}
