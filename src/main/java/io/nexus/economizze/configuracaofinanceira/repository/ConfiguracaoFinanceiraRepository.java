package io.nexus.economizze.configuracaofinanceira.repository;

import io.nexus.economizze.configuracaofinanceira.model.ConfiguracaoFinanceira;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de configuracoes financeiras do sistema.
 */
public interface ConfiguracaoFinanceiraRepository extends JpaRepository<ConfiguracaoFinanceira, Long> {

    Optional<ConfiguracaoFinanceira> findFirstByOrderByIdAsc();
}
