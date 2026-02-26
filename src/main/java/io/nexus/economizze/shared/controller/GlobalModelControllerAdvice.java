package io.nexus.economizze.shared.controller;

import io.nexus.economizze.usuario.model.Usuario;
import io.nexus.economizze.usuario.service.UsuarioService;
import java.security.Principal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Advice global para disponibilizar atributos comuns nas views.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelControllerAdvice {

    private final UsuarioService usuarioService;

    /**
     * Disponibiliza nome da aplicacao para todas as paginas.
     */
    @ModelAttribute("nomeAplicacao")
    public String nomeAplicacao() {
        return "Economizze";
    }

    /**
     * Disponibiliza data atual para uso em filtros iniciais e telas.
     */
    @ModelAttribute("dataAtual")
    public LocalDate dataAtual() {
        return LocalDate.now();
    }

    /**
     * Disponibiliza usuario autenticado para exibicao no layout.
     */
    @ModelAttribute("usuarioLogado")
    public Usuario usuarioLogado(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarioService.buscarPorUsername(principal.getName());
    }
}
