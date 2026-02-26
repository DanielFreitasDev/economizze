package io.nexus.economizze.configuracaofinanceira.model;

import io.nexus.economizze.shared.model.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuracao padrao de juros e multa aplicados em atrasos.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "configuracoes_financeiras")
public class ConfiguracaoFinanceira extends EntidadeBase {

    @Column(name = "juros_percentual_padrao", nullable = false, precision = 8, scale = 4)
    private BigDecimal jurosPercentualPadrao = BigDecimal.ZERO;

    @Column(name = "multa_percentual_padrao", nullable = false, precision = 8, scale = 4)
    private BigDecimal multaPercentualPadrao = BigDecimal.ZERO;
}
