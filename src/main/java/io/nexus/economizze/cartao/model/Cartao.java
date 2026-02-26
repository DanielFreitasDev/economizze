package io.nexus.economizze.cartao.model;

import io.nexus.economizze.shared.model.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade de cartoes de credito utilizados no controle de faturas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cartoes")
public class Cartao extends EntidadeBase {

    @Column(name = "numero_cartao", nullable = false, unique = true, length = 16)
    private String numeroCartao;

    @Column(nullable = false, length = 60)
    private String bandeira;

    @Column(nullable = false, length = 120)
    private String banco;

    @Column(name = "dia_fechamento", nullable = false)
    private Integer diaFechamento;

    @Column(name = "dia_vencimento", nullable = false)
    private Integer diaVencimento;
}
