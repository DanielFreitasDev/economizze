package io.nexus.economizze.usuario.controller;

import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.usuario.dto.AlterarSenhaForm;
import io.nexus.economizze.usuario.dto.UsuarioForm;
import io.nexus.economizze.usuario.model.PerfilUsuario;
import io.nexus.economizze.usuario.model.Usuario;
import io.nexus.economizze.usuario.service.UsuarioService;
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
 * Controller web para administracao de usuarios.
 */
@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Lista usuarios cadastrados.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    /**
     * Exibe formulario de novo usuario.
     */
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("usuarioForm")) {
            model.addAttribute("usuarioForm", new UsuarioForm());
        }
        model.addAttribute("perfis", PerfilUsuario.values());
        model.addAttribute("acao", "criar");
        return "usuarios/form";
    }

    /**
     * Salva novo usuario.
     */
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("usuarioForm") UsuarioForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("perfis", PerfilUsuario.values());
            model.addAttribute("acao", "criar");
            return "usuarios/form";
        }

        try {
            usuarioService.criar(form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuario criado com sucesso");
            return "redirect:/usuarios";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("perfis", PerfilUsuario.values());
            model.addAttribute("acao", "criar");
            return "usuarios/form";
        }
    }

    /**
     * Exibe detalhes de um usuario.
     */
    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.buscarPorId(id));
        return "usuarios/visualizar";
    }

    /**
     * Exibe formulario de edicao de usuario.
     */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);

        if (!model.containsAttribute("usuarioForm")) {
            UsuarioForm form = new UsuarioForm();
            form.setNomeCompleto(usuario.getNomeCompleto());
            form.setUsername(usuario.getUsername());
            form.setPerfil(usuario.getPerfil());
            model.addAttribute("usuarioForm", form);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", PerfilUsuario.values());
        model.addAttribute("acao", "editar");
        return "usuarios/form";
    }

    /**
     * Atualiza usuario existente.
     */
    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("usuarioForm") UsuarioForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Usuario usuario = usuarioService.buscarPorId(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("perfis", PerfilUsuario.values());
            model.addAttribute("acao", "editar");
            return "usuarios/form";
        }

        try {
            usuarioService.atualizar(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuario atualizado com sucesso");
            return "redirect:/usuarios";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("perfis", PerfilUsuario.values());
            model.addAttribute("acao", "editar");
            return "usuarios/form";
        }
    }

    /**
     * Exibe formulario de troca de senha.
     */
    @GetMapping("/{id}/senha")
    public String formularioSenha(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.buscarPorId(id));
        if (!model.containsAttribute("alterarSenhaForm")) {
            model.addAttribute("alterarSenhaForm", new AlterarSenhaForm());
        }
        return "usuarios/senha";
    }

    /**
     * Altera senha de usuario.
     */
    @PostMapping("/{id}/senha")
    public String alterarSenha(
            @PathVariable Long id,
            @Valid @ModelAttribute("alterarSenhaForm") AlterarSenhaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuarioService.buscarPorId(id));
            return "usuarios/senha";
        }

        try {
            usuarioService.alterarSenha(id, form);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha alterada com sucesso");
            return "redirect:/usuarios";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            model.addAttribute("usuario", usuarioService.buscarPorId(id));
            return "usuarios/senha";
        }
    }

    /**
     * Desativa usuario.
     */
    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.desativar(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuario desativado com sucesso");
        return "redirect:/usuarios";
    }

    /**
     * Exclui usuario.
     */
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuario excluido com sucesso");
        return "redirect:/usuarios";
    }
}
