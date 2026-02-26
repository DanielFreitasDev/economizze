package io.nexus.economizze.relatorio.dto;

import jakarta.validation.constraints.NotNull;
import java.time.YearMonth;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Formulario para filtro de relatorio analitico de pessoa.
 */
@Getter
@Setter
public class FiltroRelatorioPessoaForm {

    @NotNull(message = "Pessoa e obrigatoria")
    private Long pessoaId;

    @NotNull(message = "Competencia e obrigatoria")
    @DateTimeFormat(pattern = "yyyy-MM")
    private YearMonth competencia;
}
