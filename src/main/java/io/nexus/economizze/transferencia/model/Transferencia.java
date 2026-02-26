package io.nexus.economizze.transferencia.model;

import io.nexus.economizze.conta.model.Conta;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import io.nexus.economizze.shared.model.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade de transferencias entre contas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transferencias")
public class Transferencia extends EntidadeBase {

    @Column(nullable = false, length = 200)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_origem_id", nullable = false)
    private Conta contaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_destino_id", nullable = false)
    private Conta contaDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLancamento tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_base", nullable = false)
    private LocalDate dataBase;

    @Column(name = "data_fim_recorrencia")
    private LocalDate dataFimRecorrencia;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @Column(length = 320)
    private String observacao;
}
