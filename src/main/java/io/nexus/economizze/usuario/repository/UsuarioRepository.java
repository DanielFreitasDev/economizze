package io.nexus.economizze.usuario.repository;

import io.nexus.economizze.usuario.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de usuarios do sistema.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<Usuario> findAllByOrderByNomeCompletoAsc();
}
