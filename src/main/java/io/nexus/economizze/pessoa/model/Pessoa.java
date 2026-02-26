package io.nexus.economizze.pessoa.model;

import io.nexus.economizze.shared.model.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade de cadastro de pessoas que utilizam os cartoes do titular.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pessoas")
public class Pessoa extends EntidadeBase {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(length = 180)
    private String logradouro;

    @Column(length = 30)
    private String numero;

    @Column(length = 120)
    private String complemento;

    @Column(name = "ponto_referencia", length = 180)
    private String pontoReferencia;

    @Column(length = 120)
    private String bairro;

    @Column(length = 120)
    private String cidade;

    @Column(length = 60)
    private String estado;

    @Column(length = 8)
    private String cep;

    @Column(name = "telefone_celular", length = 20)
    private String telefoneCelular;

    @Column(length = 20)
    private String whatsapp;

    @Column(length = 180)
    private String email;
}
