package io.nexus.economizze.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Tratamento de excecoes para fluxo MVC com renderizacao de pagina amigavel.
 */
@ControllerAdvice(basePackages = {
        "io.nexus.economizze.shared.controller",
        "io.nexus.economizze.dashboard.controller",
        "io.nexus.economizze.usuario.controller",
        "io.nexus.economizze.pessoa.controller",
        "io.nexus.economizze.categoria.controller",
        "io.nexus.economizze.conta.controller",
        "io.nexus.economizze.cartao.controller",
        "io.nexus.economizze.lancamento.controller",
        "io.nexus.economizze.transferencia.controller",
        "io.nexus.economizze.cobranca.controller",
        "io.nexus.economizze.configuracaofinanceira.controller",
        "io.nexus.economizze.relatorio.controller"
})
public class WebExceptionHandler {

    /**
     * Renderiza pagina generica de erro para falhas de negocio ou infraestrutura.
     */
    @ExceptionHandler(Exception.class)
    public String tratarErro(Exception ex, HttpServletRequest request, Model model) {
        model.addAttribute("mensagemErro", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "erro";
    }
}
