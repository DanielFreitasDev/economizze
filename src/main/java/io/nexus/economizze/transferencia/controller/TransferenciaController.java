package io.nexus.economizze.transferencia.controller;

import io.nexus.economizze.conta.service.ContaService;
import io.nexus.economizze.lancamento.model.TipoLancamento;
import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.transferencia.dto.TransferenciaForm;
import io.nexus.economizze.transferencia.model.Transferencia;
import io.nexus.economizze.transferencia.service.TransferenciaService;
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
 * Controller web de transferencias.
 */
@Controller
@RequestMapping("/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;
    private final ContaService contaService;

    /**
     * Lista transferencias base e ocorrencias da competencia.
     */
    @GetMapping
    public String listar(
            @RequestParam(value = "competencia", required = false) YearMonth competencia,
            Model model
    ) {
        YearMonth competenciaSelecionada = competencia == null ? YearMonth.now() : competencia;

        model.addAttribute("competencia", competenciaSelecionada);
        model.addAttribute("transferenciasBase", transferenciaService.listar(true));
        model.addAttribute("ocorrencias", transferenciaService.listarOcorrenciasDaCompetencia(competenciaSelecionada));
        return "transferencias/lista";
    }

    /**
     * Exibe formulario de nova transferencia.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("transferenciaForm")) {
            model.addAttribute("transferenciaForm", new TransferenciaForm());
        }
        adicionarCombos(model);
        model.addAttribute("acao", "criar");
        return "transferencias/form";
    }

    /**
     * Salva transferencia.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("transferenciaForm") TransferenciaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            adicionarCombos(model);
            model.addAttribute("acao", "criar");
            return "transferencias/form";
        }

        try {
            transferenciaService.criar(form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Transferencia criada com sucesso");
            return "redirect:/transferencias";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            adicionarCombos(model);
            model.addAttribute("acao", "criar");
            return "transferencias/form";
        }
    }

    /**
     * Visualiza transferencia.
     */
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("transferencia", transferenciaService.buscarPorId(id));
        return "transferencias/visualizar";
    }

    /**
     * Exibe formulario de edicao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Transferencia transferencia = transferenciaService.buscarPorId(id);

        if (!model.containsAttribute("transferenciaForm")) {
            model.addAttribute("transferenciaForm", paraForm(transferencia));
        }

        model.addAttribute("transferencia", transferencia);
        adicionarCombos(model);
        model.addAttribute("acao", "editar");
        return "transferencias/form";
    }

    /**
     * Atualiza transferencia.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("transferenciaForm") TransferenciaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Transferencia transferencia = transferenciaService.buscarPorId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("transferencia", transferencia);
            adicionarCombos(model);
            model.addAttribute("acao", "editar");
            return "transferencias/form";
        }

        try {
            transferenciaService.atualizar(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Transferencia atualizada com sucesso");
            return "redirect:/transferencias";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("transferencia", transferencia);
            adicionarCombos(model);
            model.addAttribute("acao", "editar");
            return "transferencias/form";
        }
    }

    /**
     * Desativa transferencia.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        transferenciaService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Transferencia desativada com sucesso");
        return "redirect:/transferencias";
    }

    /**
     * Exclui transferencia.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        transferenciaService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Transferencia excluida com sucesso");
        return "redirect:/transferencias";
    }

    /**
     * Carrega dados auxiliares para formulario.
     */
    private void adicionarCombos(Model model) {
        model.addAttribute("contas", contaService.listar(false));
        model.addAttribute("tipos", TipoLancamento.values());
    }

    /**
     * Converte entidade para formulario de edicao.
     */
    private TransferenciaForm paraForm(Transferencia transferencia) {
        TransferenciaForm form = new TransferenciaForm();
        form.setDescricao(transferencia.getDescricao());
        form.setContaOrigemId(transferencia.getContaOrigem().getId());
        form.setContaDestinoId(transferencia.getContaDestino().getId());
        form.setTipo(transferencia.getTipo());
        form.setValor(transferencia.getValor().toPlainString());
        form.setDataBase(transferencia.getDataBase());
        form.setDataFimRecorrencia(transferencia.getDataFimRecorrencia());
        form.setTotalParcelas(transferencia.getTotalParcelas());
        form.setObservacao(transferencia.getObservacao());
        return form;
    }
}
