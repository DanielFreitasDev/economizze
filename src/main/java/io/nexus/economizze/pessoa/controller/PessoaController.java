package io.nexus.economizze.pessoa.controller;

import io.nexus.economizze.pessoa.dto.PessoaForm;
import io.nexus.economizze.pessoa.model.Pessoa;
import io.nexus.economizze.pessoa.service.PessoaService;
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
 * Controller web de pessoas.
 */
@Controller
@RequestMapping("/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    private final PessoaService pessoaService;

    /**
     * Lista pessoas cadastradas.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pessoas", pessoaService.listar(true));
        return "pessoas/lista";
    }

    /**
     * Exibe formulario de nova pessoa.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("pessoaForm")) {
            model.addAttribute("pessoaForm", new PessoaForm());
        }
        model.addAttribute("acao", "criar");
        return "pessoas/form";
    }

    /**
     * Salva nova pessoa.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("pessoaForm") PessoaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("acao", "criar");
            return "pessoas/form";
        }

        try {
            pessoaService.criar(form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pessoa criada com sucesso");
            return "redirect:/pessoas";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("acao", "criar");
            return "pessoas/form";
        }
    }

    /**
     * Exibe tela de visualizacao de pessoa.
     */
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("pessoa", pessoaService.buscarPorId(id));
        return "pessoas/visualizar";
    }

    /**
     * Exibe formulario de edicao de pessoa.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Pessoa pessoa = pessoaService.buscarPorId(id);

        if (!model.containsAttribute("pessoaForm")) {
            PessoaForm form = paraForm(pessoa);
            model.addAttribute("pessoaForm", form);
        }

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("acao", "editar");
        return "pessoas/form";
    }

    /**
     * Atualiza pessoa existente.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("pessoaForm") PessoaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Pessoa pessoa = pessoaService.buscarPorId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("pessoa", pessoa);
            model.addAttribute("acao", "editar");
            return "pessoas/form";
        }

        try {
            pessoaService.atualizar(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pessoa atualizada com sucesso");
            return "redirect:/pessoas";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("pessoa", pessoa);
            model.addAttribute("acao", "editar");
            return "pessoas/form";
        }
    }

    /**
     * Desativa pessoa.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pessoaService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Pessoa desativada com sucesso");
        return "redirect:/pessoas";
    }

    /**
     * Exclui pessoa.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        try {
            pessoaService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Pessoa excluida com sucesso");
            return "redirect:/pessoas";
        } catch (RecursoVinculadoException ex) {
            model.addAttribute("mensagemErro", ex.getMessage());
            model.addAttribute("path", "/pessoas");
            return "erro";
        }
    }

    /**
     * Mapeia entidade para formulario de edicao.
     */
    private PessoaForm paraForm(Pessoa pessoa) {
        PessoaForm form = new PessoaForm();
        form.setNome(pessoa.getNome());
        form.setCpf(pessoa.getCpf());
        form.setLogradouro(pessoa.getLogradouro());
        form.setNumero(pessoa.getNumero());
        form.setComplemento(pessoa.getComplemento());
        form.setPontoReferencia(pessoa.getPontoReferencia());
        form.setBairro(pessoa.getBairro());
        form.setCidade(pessoa.getCidade());
        form.setEstado(pessoa.getEstado());
        form.setCep(pessoa.getCep());
        form.setTelefoneCelular(pessoa.getTelefoneCelular());
        form.setWhatsapp(pessoa.getWhatsapp());
        form.setEmail(pessoa.getEmail());
        return form;
    }
}
