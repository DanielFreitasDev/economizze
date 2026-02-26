package io.nexus.economizze.shared.util;

import io.nexus.economizze.shared.exception.RegraDeNegocioException;
import java.math.BigDecimal;

/**
 * Utilitario para parsing de valores monetarios e percentuais.
 */
public final class NumeroUtil {

    private NumeroUtil() {
    }

    /**
     * Converte string monetaria BR (ex: R$ 1.234,56) para BigDecimal.
     */
    public static BigDecimal parseMonetario(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("Valor monetario nao informado");
        }

        // Remove simbolo da moeda e espacos para padronizar a entrada.
        String limpo = valor.replace("R$", "").trim();

        // Aceita tanto formato brasileiro quanto decimal simples.
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }

        try {
            return new BigDecimal(limpo);
        } catch (NumberFormatException ex) {
            throw new RegraDeNegocioException("Valor monetario invalido: " + valor);
        }
    }

    /**
     * Converte string percentual para BigDecimal.
     */
    public static BigDecimal parsePercentual(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("Percentual nao informado");
        }
        String limpo = valor.trim().replace("%", "").replace(",", ".");
        try {
            return new BigDecimal(limpo);
        } catch (NumberFormatException ex) {
            throw new RegraDeNegocioException("Percentual invalido: " + valor);
        }
    }
}
