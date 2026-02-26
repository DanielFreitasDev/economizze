package io.nexus.economizze.cobranca.controller;

import io.nexus.economizze.cartao.service.CartaoService;
import io.nexus.economizze.categoria.service.CategoriaService;
import io.nexus.economizze.cobranca.dto.CobrancaPessoaForm;
import io.nexus.economizze.cobranca.model.CobrancaPessoa;
import io.nexus.economizze.cobranca.service.CobrancaPessoaService;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import io.nexus.economizze.pessoa.service.PessoaService;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import jakarta.validation.Valid;
import java.time.YearMonth;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller web de cobrancas por pessoa em cartoes.
 */
@Controller
@RequestMapping("/cobrancas")
@RequiredArgsConstructor
public class CobrancaPessoaController {

    private final CobrancaPessoaService cobrancaPessoaService;
    private final PessoaService pessoaService;
    private final CartaoService cartaoService;
    private final CategoriaService categoriaService;

    /**
     * Lista cobrancas base e ocorrencias da competencia.
     */
    @GetMapping
    public String listar(
            @RequestParam(value = "competencia", required = false) YearMonth competencia,
            Model model
    ) {
        YearMonth competenciaSelecionada = competencia == null ? YearMonth.now() : competencia;

        model.addAttribute("competencia", competenciaSelecionada);
        model.addAttribute("cobrancasBase", cobrancaPessoaService.listar(true));
        model.addAttribute("ocorrencias", cobrancaPessoaService.listarOcorrenciasDaCompetencia(competenciaSelecionada));
        return "cobrancas/lista";
    }

    /**
     * Exibe formulario de nova cobranca.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("cobrancaPessoaForm")) {
            model.addAttribute("cobrancaPessoaForm", new CobrancaPessoaForm());
        }
        adicionarCombos(model);
        model.addAttribute("acao", "criar");
        return "cobrancas/form";
    }

    /**
     * Salva cobranca.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("cobrancaPessoaForm") CobrancaPessoaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            adicionarCombos(model);
            model.addAttribute("acao", "criar");
            return "cobrancas/form";
        }

        try {
            cobrancaPessoaService.criar(form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cobranca criada com sucesso");
            return "redirect:/cobrancas";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            adicionarCombos(model);
            model.addAttribute("acao", "criar");
            return "cobrancas/form";
        }
    }

    /**
     * Visualiza cobranca.
     */
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("cobranca", cobrancaPessoaService.buscarPorId(id));
        return "cobrancas/visualizar";
    }

    /**
     * Exibe formulario de edicao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        CobrancaPessoa cobranca = cobrancaPessoaService.buscarPorId(id);

        if (!model.containsAttribute("cobrancaPessoaForm")) {
            model.addAttribute("cobrancaPessoaForm", paraForm(cobranca));
        }

        model.addAttribute("cobranca", cobranca);
        adicionarCombos(model);
        model.addAttribute("acao", "editar");
        return "cobrancas/form";
    }

    /**
     * Atualiza cobranca.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("cobrancaPessoaForm") CobrancaPessoaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        CobrancaPessoa cobranca = cobrancaPessoaService.buscarPorId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("cobranca", cobranca);
            adicionarCombos(model);
            model.addAttribute("acao", "editar");
            return "cobrancas/form";
        }

        try {
            cobrancaPessoaService.atualizar(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cobranca atualizada com sucesso");
            return "redirect:/cobrancas";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("cobranca", cobranca);
            adicionarCombos(model);
            model.addAttribute("acao", "editar");
            return "cobrancas/form";
        }
    }

    /**
     * Desativa cobranca.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        cobrancaPessoaService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cobranca desativada com sucesso");
        return "redirect:/cobrancas";
    }

    /**
     * Exclui cobranca.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        cobrancaPessoaService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cobranca excluida com sucesso");
        return "redirect:/cobrancas";
    }

    /**
     * Carrega dados auxiliares para formulario.
     */
    private void adicionarCombos(Model model) {
        model.addAttribute("pessoas", pessoaService.listar(false));
        model.addAttribute("cartoes", cartaoService.listar(false));
        model.addAttribute("categorias", categoriaService.listar(false));
        model.addAttribute("tipos", TipoLancamento.values());
    }

    /**
     * Converte entidade para formulario.
     */
    private CobrancaPessoaForm paraForm(CobrancaPessoa cobranca) {
        CobrancaPessoaForm form = new CobrancaPessoaForm();
        form.setPessoaId(cobranca.getPessoa().getId());
        form.setCartaoId(cobranca.getCartao().getId());
        form.setCategoriaId(cobranca.getCategoria() == null ? null : cobranca.getCategoria().getId());
        form.setDescricao(cobranca.getDescricao());
        form.setTipo(cobranca.getTipo());
        form.setValor(cobranca.getValor().toPlainString());
        form.setDataBase(cobranca.getDataBase());
        form.setDataFimRecorrencia(cobranca.getDataFimRecorrencia());
        form.setTotalParcelas(cobranca.getTotalParcelas());
        form.setCompetenciaFaturaInicial(cobranca.getCompetenciaFaturaInicial());
        form.setObservacao(cobranca.getObservacao());
        return form;
    }
}
