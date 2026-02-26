package io.nexus.economizze.shared.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estrutura padronizada de erro exposta pela API REST.
 */
public record RespostaErroApi(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
