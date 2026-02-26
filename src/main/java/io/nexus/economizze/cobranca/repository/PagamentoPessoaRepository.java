package io.nexus.economizze.cobranca.repository;

import io.nexus.economizze.cobranca.model.PagamentoPessoa;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de pagamentos e encargos de pessoas.
 */
public interface PagamentoPessoaRepository extends JpaRepository<PagamentoPessoa, Long> {

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<PagamentoPessoa> findAllByAtivoTrueOrderByDataPagamentoDescDescricaoAsc();

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<PagamentoPessoa> findAllByOrderByDataPagamentoDescDescricaoAsc();

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<PagamentoPessoa> findAllByAtivoTrueAndDataPagamentoLessThanEqualOrderByDataPagamentoAsc(LocalDate data);

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<PagamentoPessoa> findAllByAtivoTrueAndPessoaIdOrderByDataPagamentoAsc(Long pessoaId);

    @EntityGraph(attributePaths = {"pessoa", "cartao"})
    List<PagamentoPessoa> findAllByAtivoTrueAndPessoaIdAndCartaoIdOrderByDataPagamentoAsc(Long pessoaId, Long cartaoId);

    boolean existsByPessoaId(Long pessoaId);

    boolean existsByCartaoId(Long cartaoId);
}
