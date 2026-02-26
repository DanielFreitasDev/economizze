package io.nexus.economizze.configuracaofinanceira.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de configuracao de juros e multa padrao.
 */
@Getter
@Setter
public class ConfiguracaoFinanceiraForm {

    @NotBlank(message = "Juros percentual padrao e obrigatorio")
    private String jurosPercentualPadrao;

    @NotBlank(message = "Multa percentual padrao e obrigatorio")
    private String multaPercentualPadrao;
}
