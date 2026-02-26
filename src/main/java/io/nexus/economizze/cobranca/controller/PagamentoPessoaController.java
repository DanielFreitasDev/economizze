package io.nexus.economizze.cobranca.controller;

import io.nexus.economizze.cartao.service.CartaoService;
import io.nexus.economizze.cobranca.dto.PagamentoPessoaForm;
import io.nexus.economizze.cobranca.model.PagamentoPessoa;
import io.nexus.economizze.cobranca.model.TipoMovimentoPessoa;
import io.nexus.economizze.cobranca.service.PagamentoPessoaService;
import io.nexus.economizze.pessoa.service.PessoaService;
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
 * Controller web de pagamentos e encargos de pessoas.
 */
@Controller
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PagamentoPessoaController {

    private final PagamentoPessoaService pagamentoPessoaService;
    private final PessoaService pessoaService;
    private final CartaoService cartaoService;

    /**
     * Lista movimentos de pagamento/encargo.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pagamentos", pagamentoPessoaService.listar(true));
        return "pagamentos/lista";
    }

    /**
     * Exibe formulario de novo movimento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("pagamentoPessoaForm")) {
            model.addAttribute("pagamentoPessoaForm", new PagamentoPessoaForm());
        }
        adicionarCombos(model);
        model.addAttribute("acao", "criar");
        return "pagamentos/form";
    }

    /**
     * Salva novo movimento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("pagamentoPessoaForm") PagamentoPessoaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            adicionarCombos(model);
            model.addAttribute("acao", "criar");
            return "pagamentos/form";
        }

        pagamentoPessoaService.criar(form);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimento criado com sucesso");
        return "redirect:/pagamentos";
    }

    /**
     * Visualiza movimento.
     */
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("pagamento", pagamentoPessoaService.buscarPorId(id));
        return "pagamentos/visualizar";
    }

    /**
     * Exibe formulario de edicao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        PagamentoPessoa pagamento = pagamentoPessoaService.buscarPorId(id);

        if (!model.containsAttribute("pagamentoPessoaForm")) {
            model.addAttribute("pagamentoPessoaForm", paraForm(pagamento));
        }

        model.addAttribute("pagamento", pagamento);
        adicionarCombos(model);
        model.addAttribute("acao", "editar");
        return "pagamentos/form";
    }

    /**
     * Atualiza movimento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("pagamentoPessoaForm") PagamentoPessoaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        PagamentoPessoa pagamento = pagamentoPessoaService.buscarPorId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("pagamento", pagamento);
            adicionarCombos(model);
            model.addAttribute("acao", "editar");
            return "pagamentos/form";
        }

        pagamentoPessoaService.atualizar(id, form);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimento atualizado com sucesso");
        return "redirect:/pagamentos";
    }

    /**
     * Desativa movimento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pagamentoPessoaService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimento desativado com sucesso");
        return "redirect:/pagamentos";
    }

    /**
     * Exclui movimento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pagamentoPessoaService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimento excluido com sucesso");
        return "redirect:/pagamentos";
    }

    /**
     * Carrega dados auxiliares de formulario.
     */
    private void adicionarCombos(Model model) {
        model.addAttribute("pessoas", pessoaService.listar(false));
        model.addAttribute("cartoes", cartaoService.listar(false));
        model.addAttribute("tiposMovimento", TipoMovimentoPessoa.values());
    }

    /**
     * Converte entidade para formulario de edicao.
     */
    private PagamentoPessoaForm paraForm(PagamentoPessoa pagamento) {
        PagamentoPessoaForm form = new PagamentoPessoaForm();
        form.setPessoaId(pagamento.getPessoa().getId());
        form.setCartaoId(pagamento.getCartao().getId());
        form.setDescricao(pagamento.getDescricao());
        form.setTipo(pagamento.getTipo());
        form.setValor(pagamento.getValor().toPlainString());
        form.setDataPagamento(pagamento.getDataPagamento());
        return form;
    }
}
