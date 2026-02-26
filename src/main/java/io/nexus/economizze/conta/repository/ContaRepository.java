package io.nexus.economizze.conta.repository;

import io.nexus.economizze.conta.model.Conta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de contas financeiras.
 */
public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findAllByAtivoTrueOrderByNomeAsc();

    List<Conta> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
