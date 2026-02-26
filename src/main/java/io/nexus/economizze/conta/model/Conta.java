package io.nexus.economizze.conta.model;

import io.nexus.economizze.shared.model.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa contas financeiras (bancarias ou carteira).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "contas")
public class Conta extends EntidadeBase {

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @Column(length = 120)
    private String banco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false, length = 40)
    private TipoConta tipoConta;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial = BigDecimal.ZERO;
}
