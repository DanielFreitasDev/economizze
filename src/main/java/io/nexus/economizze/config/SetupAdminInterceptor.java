package io.nexus.economizze.config;

import io.nexus.economizze.usuario.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que obriga a criacao do primeiro administrador antes do uso do sistema.
 */
@RequiredArgsConstructor
public class SetupAdminInterceptor implements HandlerInterceptor {

    private final UsuarioService usuarioService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Se ja existe usuario, o sistema segue fluxo normal sem bloqueio.
        if (usuarioService.existeUsuarios()) {
            return true;
        }

        String uri = request.getRequestURI();

        // Permite recursos publicos e a rota de setup inicial.
        if (uri.startsWith("/setup")
                || uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/images")
                || uri.startsWith("/error")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")) {
            return true;
        }

        // Redireciona qualquer acesso para o setup quando nao ha usuarios cadastrados.
        response.sendRedirect("/setup");
        return false;
    }
}
