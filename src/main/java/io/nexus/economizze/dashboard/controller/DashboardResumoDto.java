package io.nexus.economizze.dashboard.controller;

import java.math.BigDecimal;

/**
 * DTO para totalizadores principais exibidos no dashboard.
 */
public record DashboardResumoDto(
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal totalTransferencias,
        BigDecimal totalCobrancasTerceiros,
        BigDecimal minhaParcelaFatura
) {
}
