package io.nexus.economizze.cobranca.repository;

import io.nexus.economizze.cobranca.model.CobrancaPessoa;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de cobrancas de terceiros em cartoes.
 */
public interface CobrancaPessoaRepository extends JpaRepository<CobrancaPessoa, Long> {

    @EntityGraph(attributePaths = {"pessoa", "cartao", "categoria"})
    List<CobrancaPessoa> findAllByAtivoTrueOrderByDataBaseDescDescricaoAsc();

    @EntityGraph(attributePaths = {"pessoa", "cartao", "categoria"})
    List<CobrancaPessoa> findAllByOrderByDataBaseDescDescricaoAsc();

    @EntityGraph(attributePaths = {"pessoa", "cartao", "categoria"})
    List<CobrancaPessoa> findAllByAtivoTrueAndDataBaseLessThanEqualOrderByDataBaseAscDescricaoAsc(LocalDate data);

    boolean existsByPessoaId(Long pessoaId);

    boolean existsByCartaoId(Long cartaoId);

    boolean existsByCategoriaId(Long categoriaId);
}
