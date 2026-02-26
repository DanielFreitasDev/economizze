package io.nexus.economizze.lancamento.model;

import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.categoria.model.Categoria;
import io.nexus.economizze.conta.model.Conta;
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
 * Entidade com regras de receitas e despesas para geracao do fluxo mensal.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lancamentos")
public class Lancamento extends EntidadeBase {

    @Column(nullable = false, length = 180)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NaturezaLancamento natureza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLancamento tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartao_id")
    private Cartao cartao;

    @Column(name = "data_base", nullable = false)
    private LocalDate dataBase;

    @Column(name = "data_fim_recorrencia")
    private LocalDate dataFimRecorrencia;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @Column(name = "competencia_fatura_inicial")
    private LocalDate competenciaFaturaInicial;

    @Column(length = 320)
    private String observacao;
}
