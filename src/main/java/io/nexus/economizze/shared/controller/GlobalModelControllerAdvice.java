package io.nexus.economizze.shared.controller;

import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * Disponibiliza o caminho atual da requisicao para controle de menu ativo nas views.
     *
     * <p>Esse atributo evita dependencia de objetos internos do motor de template, mantendo
     * compatibilidade entre versoes do Thymeleaf/Spring.</p>
     *
     * @param requisicaoHttp requisicao HTTP atual recebida pelo Spring MVC
     * @return caminho da URL atual (ex.: "/dashboard"), ou string vazia quando nao existir
     */
    @ModelAttribute("caminhoAtual")
    public String caminhoAtual(HttpServletRequest requisicaoHttp) {
        // Protege as views de cenarios sem request valido, evitando NullPointer em expressoes.
        if (requisicaoHttp == null || requisicaoHttp.getRequestURI() == null) {
            return "";
        }

        // Retorna o caminho corrente para destacar o item correto no menu de navegacao.
        return requisicaoHttp.getRequestURI();
    }
}
