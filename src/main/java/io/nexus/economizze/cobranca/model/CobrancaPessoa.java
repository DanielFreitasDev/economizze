package io.nexus.economizze.cobranca.model;

import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.categoria.model.Categoria;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import io.nexus.economizze.pessoa.model.Pessoa;
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
 * Entidade de cobrancas por pessoa em cartoes emprestados.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cobrancas_pessoas")
public class CobrancaPessoa extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartao_id", nullable = false)
    private Cartao cartao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false, length = 200)
    private String descricao;

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

    @Column(name = "competencia_fatura_inicial")
    private LocalDate competenciaFaturaInicial;

    @Column(length = 320)
    private String observacao;
}
