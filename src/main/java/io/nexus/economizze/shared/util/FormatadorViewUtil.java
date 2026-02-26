package io.nexus.economizze.shared.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Utilitario exposto para uso em templates Thymeleaf.
 */
@Component("formatador")
public class FormatadorViewUtil {

    private static final Locale LOCALE_PT_BR = Locale.of("pt", "BR");

    /**
     * Aplica mascara de CPF para exibir no formato XXX.XXX.XXX-XX.
     */
    public String maskCpf(String cpf) {
        String digitos = TextoUtil.manterSomenteDigitos(cpf);
        if (digitos == null || digitos.length() != 11) {
            return cpf;
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    /**
     * Aplica mascara de cartao para exibir no formato XXXX XXXX XXXX XXXX.
     */
    public String maskCartao(String numeroCartao) {
        String digitos = TextoUtil.manterSomenteDigitos(numeroCartao);
        if (digitos == null || digitos.length() != 16) {
            return numeroCartao;
        }
        return digitos.substring(0, 4) + " " + digitos.substring(4, 8) + " " + digitos.substring(8, 12) + " " + digitos.substring(12);
    }

    /**
     * Formata valores no padrao monetario brasileiro.
     */
    public String moeda(BigDecimal valor) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE_PT_BR);
        return formatter.format(valor == null ? BigDecimal.ZERO : valor);
    }
}
