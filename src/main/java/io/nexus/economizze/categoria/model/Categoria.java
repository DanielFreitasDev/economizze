package io.nexus.economizze.categoria.model;

import io.nexus.economizze.shared.model.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade de categorias para classificacao de receitas, despesas e cobrancas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categorias")
public class Categoria extends EntidadeBase {

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @Column(length = 220)
    private String descricao;
}
