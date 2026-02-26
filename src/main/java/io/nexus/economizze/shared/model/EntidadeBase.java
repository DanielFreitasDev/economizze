package io.nexus.economizze.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe base com colunas comuns para todas as entidades persistidas.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean ativo = Boolean.TRUE;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    /**
     * Preenche os timestamps de criacao e atualizacao antes do primeiro insert.
     */
    @PrePersist
    public void prePersist() {
        // Mantem o mesmo horario no cadastro inicial para facilitar rastreabilidade.
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    /**
     * Atualiza o timestamp de modificacao sempre que a entidade for alterada.
     */
    @PreUpdate
    public void preUpdate() {
        // O campo de atualizacao registra a ultima alteracao efetiva da entidade.
        this.atualizadoEm = LocalDateTime.now();
    }
}
