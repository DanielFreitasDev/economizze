package io.nexus.economizze.lancamento.dto;

import io.nexus.economizze.lancamento.model.NaturezaLancamento;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Formulario de cadastro e edicao de lancamentos.
 */
@Getter
@Setter
public class LancamentoForm {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 180)
    private String nome;

    @NotNull(message = "Natureza e obrigatoria")
    private NaturezaLancamento natureza;

    @NotNull(message = "Tipo e obrigatorio")
    private TipoLancamento tipo;

    @NotBlank(message = "Valor e obrigatorio")
    private String valor;

    @NotNull(message = "Categoria e obrigatoria")
    private Long categoriaId;

    private Long contaId;

    private Long cartaoId;

    @NotNull(message = "Data e obrigatoria")
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
