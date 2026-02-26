package io.nexus.economizze.lancamento.controller;

import io.nexus.economizze.cartao.service.CartaoService;
import io.nexus.economizze.categoria.service.CategoriaService;
import io.nexus.economizze.conta.service.ContaService;
import io.nexus.economizze.lancamento.dto.LancamentoForm;
import io.nexus.economizze.lancamento.model.Lancamento;
import io.nexus.economizze.lancamento.service.LancamentoService;
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
 * Controller web de lancamentos financeiros.
 */
@Controller
@RequestMapping("/lancamentos")
@RequiredArgsConstructor
public class LancamentoController {

    private final LancamentoService lancamentoService;
    private final CategoriaService categoriaService;
    private final ContaService contaService;
    private final CartaoService cartaoService;

    /**
     * Lista lancamentos base e ocorrencias da competencia.
     */
    @GetMapping
    public String listar(
            @RequestParam(value = "competencia", required = false) YearMonth competencia,
            Model model
    ) {
        YearMonth competenciaSelecionada = competencia == null ? YearMonth.now() : competencia;

        model.addAttribute("competencia", competenciaSelecionada);
        model.addAttribute("lancamentosBase", lancamentoService.listar(true));
        model.addAttribute("ocorrencias", lancamentoService.listarOcorrenciasDaCompetencia(competenciaSelecionada));
        return "lancamentos/lista";
    }

    /**
     * Exibe formulario de novo lancamento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("lancamentoForm")) {
            model.addAttribute("lancamentoForm", new LancamentoForm());
        }
        adicionarCombos(model);
        model.addAttribute("acao", "criar");
        return "lancamentos/form";
    }

    /**
     * Salva novo lancamento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("lancamentoForm") LancamentoForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            adicionarCombos(model);
            model.addAttribute("acao", "criar");
            return "lancamentos/form";
        }

        try {
            lancamentoService.criar(form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Lancamento criado com sucesso");
            return "redirect:/lancamentos";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            adicionarCombos(model);
            model.addAttribute("acao", "criar");
            return "lancamentos/form";
        }
    }

    /**
     * Visualiza lancamento.
     */
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("lancamento", lancamentoService.buscarPorId(id));
        return "lancamentos/visualizar";
    }

    /**
     * Exibe formulario de edicao de lancamento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Lancamento lancamento = lancamentoService.buscarPorId(id);

        if (!model.containsAttribute("lancamentoForm")) {
            model.addAttribute("lancamentoForm", paraForm(lancamento));
        }

        model.addAttribute("lancamento", lancamento);
        adicionarCombos(model);
        model.addAttribute("acao", "editar");
        return "lancamentos/form";
    }

    /**
     * Atualiza lancamento existente.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("lancamentoForm") LancamentoForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Lancamento lancamento = lancamentoService.buscarPorId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("lancamento", lancamento);
            adicionarCombos(model);
            model.addAttribute("acao", "editar");
            return "lancamentos/form";
        }

        try {
            lancamentoService.atualizar(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Lancamento atualizado com sucesso");
            return "redirect:/lancamentos";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("lancamento", lancamento);
            adicionarCombos(model);
            model.addAttribute("acao", "editar");
            return "lancamentos/form";
        }
    }

    /**
     * Desativa lancamento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        lancamentoService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Lancamento desativado com sucesso");
        return "redirect:/lancamentos";
    }

    /**
     * Exclui lancamento.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        lancamentoService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Lancamento excluido com sucesso");
        return "redirect:/lancamentos";
    }

    /**
     * Carrega dados auxiliares de selecao para formulario.
     */
    private void adicionarCombos(Model model) {
        model.addAttribute("categorias", categoriaService.listar(false));
        model.addAttribute("contas", contaService.listar(false));
        model.addAttribute("cartoes", cartaoService.listar(false));
        model.addAttribute("tipos", io.nexus.economizze.lancamento.model.TipoLancamento.values());
        model.addAttribute("naturezas", io.nexus.economizze.lancamento.model.NaturezaLancamento.values());
    }

    /**
     * Converte entidade para formulario de edicao.
     */
    private LancamentoForm paraForm(Lancamento lancamento) {
        LancamentoForm form = new LancamentoForm();
        form.setNome(lancamento.getNome());
        form.setNatureza(lancamento.getNatureza());
        form.setTipo(lancamento.getTipo());
        form.setValor(lancamento.getValor().toPlainString());
        form.setCategoriaId(lancamento.getCategoria().getId());
        form.setContaId(lancamento.getConta() == null ? null : lancamento.getConta().getId());
        form.setCartaoId(lancamento.getCartao() == null ? null : lancamento.getCartao().getId());
        form.setDataBase(lancamento.getDataBase());
        form.setDataFimRecorrencia(lancamento.getDataFimRecorrencia());
        form.setTotalParcelas(lancamento.getTotalParcelas());
        form.setCompetenciaFaturaInicial(lancamento.getCompetenciaFaturaInicial());
        form.setObservacao(lancamento.getObservacao());
        return form;
    }
}
