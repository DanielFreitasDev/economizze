package io.nexus.economizze.config;

import io.nexus.economizze.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracoes gerais de MVC, conversores e interceptadores.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ConversorStringParaYearMonth conversorStringParaYearMonth;
    private final ConversorYearMonthParaString conversorYearMonthParaString;
    private final UsuarioService usuarioService;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(conversorStringParaYearMonth);
        registry.addConverter(conversorYearMonthParaString);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SetupAdminInterceptor(usuarioService));
    }
}
