package io.nexus.economizze.cartao.repository;

import io.nexus.economizze.cartao.model.Cartao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de cartoes de credito.
 */
public interface CartaoRepository extends JpaRepository<Cartao, Long> {

    List<Cartao> findAllByAtivoTrueOrderByBancoAscNumeroCartaoAsc();

    List<Cartao> findAllByOrderByBancoAscNumeroCartaoAsc();

    boolean existsByNumeroCartao(String numeroCartao);

    boolean existsByNumeroCartaoAndIdNot(String numeroCartao, Long id);

    Optional<Cartao> findByNumeroCartao(String numeroCartao);
}
