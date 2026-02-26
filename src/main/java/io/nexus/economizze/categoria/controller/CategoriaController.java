package io.nexus.economizze.categoria.controller;

import io.nexus.economizze.categoria.dto.CategoriaForm;
import io.nexus.economizze.categoria.model.Categoria;
import io.nexus.economizze.categoria.service.CategoriaService;
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
 * Controller web de categorias.
 */
@Controller
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Lista categorias cadastradas.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listar(true));
        return "categorias/lista";
    }

    /**
     * Exibe formulario de nova categoria.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("categoriaForm")) {
            model.addAttribute("categoriaForm", new CategoriaForm());
        }
        model.addAttribute("acao", "criar");
        return "categorias/form";
    }

    /**
     * Salva categoria.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("categoriaForm") CategoriaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("acao", "criar");
            return "categorias/form";
        }

        try {
            categoriaService.criar(form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria criada com sucesso");
            return "redirect:/categorias";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("acao", "criar");
            return "categorias/form";
        }
    }

    /**
     * Visualiza categoria.
     */
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", categoriaService.buscarPorId(id));
        return "categorias/visualizar";
    }

    /**
     * Exibe formulario de edicao.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.buscarPorId(id);

        if (!model.containsAttribute("categoriaForm")) {
            CategoriaForm form = new CategoriaForm();
            form.setNome(categoria.getNome());
            form.setDescricao(categoria.getDescricao());
            model.addAttribute("categoriaForm", form);
        }

        model.addAttribute("categoria", categoria);
        model.addAttribute("acao", "editar");
        return "categorias/form";
    }

    /**
     * Atualiza categoria.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoriaForm") CategoriaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Categoria categoria = categoriaService.buscarPorId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("categoria", categoria);
            model.addAttribute("acao", "editar");
            return "categorias/form";
        }

        try {
            categoriaService.atualizar(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria atualizada com sucesso");
            return "redirect:/categorias";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("categoria", categoria);
            model.addAttribute("acao", "editar");
            return "categorias/form";
        }
    }

    /**
     * Desativa categoria.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoriaService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria desativada com sucesso");
        return "redirect:/categorias";
    }

    /**
     * Exclui categoria.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        try {
            categoriaService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Categoria excluida com sucesso");
            return "redirect:/categorias";
        } catch (RecursoVinculadoException ex) {
            model.addAttribute("mensagemErro", ex.getMessage());
            model.addAttribute("path", "/categorias");
            return "erro";
        }
    }
}
