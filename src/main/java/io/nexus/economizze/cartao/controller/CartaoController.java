package io.nexus.economizze.cartao.controller;

import io.nexus.economizze.cartao.dto.CartaoForm;
import io.nexus.economizze.cartao.model.Cartao;
import io.nexus.economizze.cartao.service.CartaoService;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.shared.exception.RecursoVinculadoException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller web de cartoes.
 */
@Controller
@RequestMapping("/cartoes")
@RequiredArgsConstructor
public class CartaoController {

    private final CartaoService cartaoService;

    /**
     * Lista cartoes cadastrados.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cartoes", cartaoService.listar(true));
        return "cartoes/lista";
    }

    /**
     * Exibe formulario de novo cartao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("cartaoForm")) {
            model.addAttribute("cartaoForm", new CartaoForm());
        }
        model.addAttribute("acao", "criar");
        return "cartoes/form";
    }

    /**
     * Salva cartao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("cartaoForm") CartaoForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("acao", "criar");
            return "cartoes/form";
        }

        try {
            cartaoService.criar(form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cartao criado com sucesso");
            return "redirect:/cartoes";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("acao", "criar");
            return "cartoes/form";
        }
    }

    /**
     * Visualiza cartao.
     */
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("cartao", cartaoService.buscarPorId(id));
        return "cartoes/visualizar";
    }

    /**
     * Exibe formulario de edicao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Cartao cartao = cartaoService.buscarPorId(id);

        if (!model.containsAttribute("cartaoForm")) {
            CartaoForm form = new CartaoForm();
            form.setNumeroCartao(cartao.getNumeroCartao());
            form.setBandeira(cartao.getBandeira());
            form.setBanco(cartao.getBanco());
            form.setDiaFechamento(cartao.getDiaFechamento());
            form.setDiaVencimento(cartao.getDiaVencimento());
            model.addAttribute("cartaoForm", form);
        }

        model.addAttribute("cartao", cartao);
        model.addAttribute("acao", "editar");
        return "cartoes/form";
    }

    /**
     * Atualiza cartao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("cartaoForm") CartaoForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Cartao cartao = cartaoService.buscarPorId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("cartao", cartao);
            model.addAttribute("acao", "editar");
            return "cartoes/form";
        }

        try {
            cartaoService.atualizar(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cartao atualizado com sucesso");
            return "redirect:/cartoes";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("cartao", cartao);
            model.addAttribute("acao", "editar");
            return "cartoes/form";
        }
    }

    /**
     * Desativa cartao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        cartaoService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cartao desativado com sucesso");
        return "redirect:/cartoes";
    }

    /**
     * Exclui cartao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        try {
            cartaoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cartao excluido com sucesso");
            return "redirect:/cartoes";
        } catch (RecursoVinculadoException ex) {
            model.addAttribute("mensagemErro", ex.getMessage());
            model.addAttribute("path", "/cartoes");
            return "erro";
        }
    }

    /**
     * Endpoint para frontend obter as 5 competencias de fatura do cartao.
     */
    @GetMapping("/{id}/competencias")
    @ResponseBody
    public List<YearMonth> listarCompetencias(@PathVariable Long id) {
        return cartaoService.listarCincoCompetencias(id, LocalDate.now());
    }
}
