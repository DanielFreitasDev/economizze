package io.nexus.economizze.usuario.controller;

import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import io.nexus.economizze.usuario.dto.SetupAdminForm;
import io.nexus.economizze.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller de autenticacao e setup inicial do primeiro administrador.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    /**
     * Renderiza pagina de login do sistema.
     */
    @GetMapping("/login")
    public String login() {
        if (!usuarioService.existeUsuarios()) {
            return "redirect:/setup";
        }
        return "login";
    }

    /**
     * Renderiza formulario de criacao do primeiro administrador.
     */
    @GetMapping("/setup")
    public String setup(Model model) {
        if (usuarioService.existeUsuarios()) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("setupAdminForm")) {
            model.addAttribute("setupAdminForm", new SetupAdminForm());
        }
        return "setup";
    }

    /**
     * Processa criacao do primeiro administrador do sistema.
     */
    @PostMapping("/setup")
    public String criarPrimeiroAdmin(
            @Valid @ModelAttribute("setupAdminForm") SetupAdminForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "setup";
        }

        try {
            usuarioService.criarPrimeiroAdmin(form);
            log.info("Setup inicial concluido com sucesso");
            return "redirect:/login?setupConcluido";
        } catch (RegraDeNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            return "setup";
        }
    }
}
