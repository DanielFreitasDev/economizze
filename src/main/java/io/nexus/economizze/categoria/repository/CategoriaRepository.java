package io.nexus.economizze.categoria.repository;

import io.nexus.economizze.categoria.model.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de categorias.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByAtivoTrueOrderByNomeAsc();

    List<Categoria> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
