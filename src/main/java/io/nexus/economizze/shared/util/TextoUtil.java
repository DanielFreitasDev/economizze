package io.nexus.economizze.shared.util;

import java.util.Locale;

/**
 * Utilitario para padronizacao de textos e valores numericos em formato string.
 */
public final class TextoUtil {

    private TextoUtil() {
    }

    /**
     * Converte para maiusculo em pt-BR, preservando nulo.
     */
    public static String paraMaiusculo(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.trim().toUpperCase(Locale.of("pt", "BR"));
    }

    /**
     * Remove qualquer caractere que nao seja digito.
     */
    public static String manterSomenteDigitos(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }
}
