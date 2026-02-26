package io.nexus.economizze.conta.controller;

import io.nexus.economizze.conta.dto.ContaForm;
import io.nexus.economizze.conta.model.Conta;
import io.nexus.economizze.conta.model.TipoConta;
import io.nexus.economizze.conta.service.ContaService;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.shared.exception.RecursoVinculadoException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller web de contas.
 */
@Controller
@RequestMapping("/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService contaService;

    /**
     * Lista contas cadastradas.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("contas", contaService.listar(true));
        return "contas/lista";
    }

    /**
     * Exibe formulario de nova conta.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("contaForm")) {
            model.addAttribute("contaForm", new ContaForm());
        }
        model.addAttribute("tiposConta", TipoConta.values());
        model.addAttribute("acao", "criar");
        return "contas/form";
    }

    /**
     * Salva conta.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("contaForm") ContaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposConta", TipoConta.values());
            model.addAttribute("acao", "criar");
            return "contas/form";
        }

        try {
            contaService.criar(form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Conta criada com sucesso");
            return "redirect:/contas";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("tiposConta", TipoConta.values());
            model.addAttribute("acao", "criar");
            return "contas/form";
        }
    }

    /**
     * Visualiza conta.
     */
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("conta", contaService.buscarPorId(id));
        return "contas/visualizar";
    }

    /**
     * Exibe formulario de edicao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Conta conta = contaService.buscarPorId(id);

        if (!model.containsAttribute("contaForm")) {
            ContaForm form = new ContaForm();
            form.setNome(conta.getNome());
            form.setBanco(conta.getBanco());
            form.setTipoConta(conta.getTipoConta());
            form.setSaldoInicial(conta.getSaldoInicial().toPlainString());
            model.addAttribute("contaForm", form);
        }

        model.addAttribute("conta", conta);
        model.addAttribute("tiposConta", TipoConta.values());
        model.addAttribute("acao", "editar");
        return "contas/form";
    }

    /**
     * Atualiza conta.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("contaForm") ContaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Conta conta = contaService.buscarPorId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("conta", conta);
            model.addAttribute("tiposConta", TipoConta.values());
            model.addAttribute("acao", "editar");
            return "contas/form";
        }

        try {
            contaService.atualizar(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Conta atualizada com sucesso");
            return "redirect:/contas";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("conta", conta);
            model.addAttribute("tiposConta", TipoConta.values());
            model.addAttribute("acao", "editar");
            return "contas/form";
        }
    }

    /**
     * Desativa conta.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contaService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Conta desativada com sucesso");
        return "redirect:/contas";
    }

    /**
     * Exclui conta.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        try {
            contaService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Conta excluida com sucesso");
            return "redirect:/contas";
        } catch (RecursoVinculadoException ex) {
            model.addAttribute("mensagemErro", ex.getMessage());
            model.addAttribute("path", "/contas");
            return "erro";
        }
    }
}
