package io.nexus.economizze.lancamento.repository;

import io.nexus.economizze.lancamento.model.Lancamento;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de lancamentos financeiros.
 */
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    @EntityGraph(attributePaths = {"categoria", "conta", "cartao"})
    List<Lancamento> findAllByAtivoTrueOrderByDataBaseDescNomeAsc();

    @EntityGraph(attributePaths = {"categoria", "conta", "cartao"})
    List<Lancamento> findAllByOrderByDataBaseDescNomeAsc();

    @EntityGraph(attributePaths = {"categoria", "conta", "cartao"})
    List<Lancamento> findAllByAtivoTrueAndDataBaseLessThanEqualOrderByDataBaseAscNomeAsc(LocalDate data);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByContaId(Long contaId);

    boolean existsByCartaoId(Long cartaoId);
}
