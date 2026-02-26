package io.nexus.economizze.transferencia.dto;

import io.nexus.economizze.lancamento.model.TipoLancamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Formulario de cadastro e edicao de transferencias.
 */
@Getter
@Setter
public class TransferenciaForm {

    @NotBlank(message = "Descricao e obrigatoria")
    @Size(max = 200)
    private String descricao;

    @NotNull(message = "Conta de origem e obrigatoria")
    private Long contaOrigemId;

    @NotNull(message = "Conta de destino e obrigatoria")
    private Long contaDestinoId;

    @NotNull(message = "Tipo e obrigatorio")
    private TipoLancamento tipo;

    @NotBlank(message = "Valor e obrigatorio")
    private String valor;

    @NotNull(message = "Data e obrigatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataBase;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimRecorrencia;

    private Integer totalParcelas;

    @Size(max = 320)
    private String observacao;
}
