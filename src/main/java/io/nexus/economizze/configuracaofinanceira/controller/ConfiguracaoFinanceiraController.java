package io.nexus.economizze.configuracaofinanceira.controller;

import io.nexus.economizze.configuracaofinanceira.dto.ConfiguracaoFinanceiraForm;
import io.nexus.economizze.configuracaofinanceira.model.ConfiguracaoFinanceira;
import io.nexus.economizze.configuracaofinanceira.service.ConfiguracaoFinanceiraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller web para configuracoes globais de juros e multa.
 */
@Controller
@RequestMapping("/configuracoes/financeiras")
@RequiredArgsConstructor
public class ConfiguracaoFinanceiraController {

    private final ConfiguracaoFinanceiraService configuracaoFinanceiraService;

    /**
     * Exibe formulario com configuracao financeira atual.
     */
    @GetMapping
    public String exibir(Model model) {
        ConfiguracaoFinanceira configuracao = configuracaoFinanceiraService.buscarConfiguracaoPrincipal();

        if (!model.containsAttribute("configuracaoFinanceiraForm")) {
            ConfiguracaoFinanceiraForm form = new ConfiguracaoFinanceiraForm();
            form.setJurosPercentualPadrao(configuracao.getJurosPercentualPadrao().toPlainString());
            form.setMultaPercentualPadrao(configuracao.getMultaPercentualPadrao().toPlainString());
            model.addAttribute("configuracaoFinanceiraForm", form);
        }

        return "configuracoes/financeiras";
    }

    /**
     * Atualiza configuracao financeira global.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String atualizar(
            @Valid @ModelAttribute("configuracaoFinanceiraForm") ConfiguracaoFinanceiraForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "configuracoes/financeiras";
        }

        configuracaoFinanceiraService.atualizar(form);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Configuracoes financeiras atualizadas");
        return "redirect:/configuracoes/financeiras";
    }
}
