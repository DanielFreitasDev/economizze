package io.nexus.economizze.transferencia.repository;

import io.nexus.economizze.transferencia.model.Transferencia;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de transferencias.
 */
public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    @EntityGraph(attributePaths = {"contaOrigem", "contaDestino"})
    List<Transferencia> findAllByAtivoTrueOrderByDataBaseDescDescricaoAsc();

    @EntityGraph(attributePaths = {"contaOrigem", "contaDestino"})
    List<Transferencia> findAllByOrderByDataBaseDescDescricaoAsc();

    @EntityGraph(attributePaths = {"contaOrigem", "contaDestino"})
    List<Transferencia> findAllByAtivoTrueAndDataBaseLessThanEqualOrderByDataBaseAscDescricaoAsc(LocalDate data);

    boolean existsByContaOrigemIdOrContaDestinoId(Long contaOrigemId, Long contaDestinoId);
}
