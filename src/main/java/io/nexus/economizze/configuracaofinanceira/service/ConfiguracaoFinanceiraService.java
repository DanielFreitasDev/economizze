package io.nexus.economizze.configuracaofinanceira.service;

import io.nexus.economizze.configuracaofinanceira.dto.ConfiguracaoFinanceiraForm;
import io.nexus.economizze.configuracaofinanceira.model.ConfiguracaoFinanceira;
import io.nexus.economizze.configuracaofinanceira.repository.ConfiguracaoFinanceiraRepository;
import io.nexus.economizze.shared.exception.RecursoNaoEncontradoException;
import io.nexus.economizze.shared.util.NumeroUtil;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para configuracoes financeiras globais.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracaoFinanceiraService {

    private final ConfiguracaoFinanceiraRepository configuracaoFinanceiraRepository;

    /**
     * Retorna configuracao principal do sistema.
     */
    @Transactional(readOnly = true)
    public ConfiguracaoFinanceira buscarConfiguracaoPrincipal() {
        return configuracaoFinanceiraRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Configuracao financeira nao encontrada"));
    }

    /**
     * Atualiza valores padrao de juros e multa.
     */
    @Transactional
    public ConfiguracaoFinanceira atualizar(ConfiguracaoFinanceiraForm form) {
        ConfiguracaoFinanceira configuracao = buscarConfiguracaoPrincipal();

        BigDecimal juros = NumeroUtil.parsePercentual(form.getJurosPercentualPadrao());
        BigDecimal multa = NumeroUtil.parsePercentual(form.getMultaPercentualPadrao());

        configuracao.setJurosPercentualPadrao(juros);
        configuracao.setMultaPercentualPadrao(multa);

        ConfiguracaoFinanceira salva = configuracaoFinanceiraRepository.save(configuracao);
        log.info("Configuracoes financeiras atualizadas: juros={} multa={}", juros, multa);
        return salva;
    }
}
