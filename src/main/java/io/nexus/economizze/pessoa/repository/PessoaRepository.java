package io.nexus.economizze.pessoa.repository;

import io.nexus.economizze.pessoa.model.Pessoa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de pessoas.
 */
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    List<Pessoa> findAllByAtivoTrueOrderByNomeAsc();

    List<Pessoa> findAllByOrderByNomeAsc();

    Optional<Pessoa> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
