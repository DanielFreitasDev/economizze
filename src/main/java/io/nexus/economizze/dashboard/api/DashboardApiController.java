package io.nexus.economizze.dashboard.api;

import io.nexus.economizze.dashboard.controller.DashboardResumoDto;
import io.nexus.economizze.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST para consolidacoes do dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardApiController {

    private final DashboardService dashboardService;

    /**
     * Retorna resumo e listas de ocorrencias da competencia.
     */
    @Operation(summary = "Consultar dashboard mensal")
    @GetMapping
    public Map<String, Object> consultar(
            @RequestParam(value = "competencia", required = false) YearMonth competencia
    ) {
        YearMonth competenciaSelecionada = competencia == null ? YearMonth.now() : competencia;
        DashboardResumoDto resumo = dashboardService.calcularResumo(competenciaSelecionada);

        return Map.of(
                "competencia", competenciaSelecionada,
                "resumo", resumo,
                "lancamentos", dashboardService.listarLancamentosCompetencia(competenciaSelecionada),
                "transferencias", dashboardService.listarTransferenciasCompetencia(competenciaSelecionada),
                "cobrancas", dashboardService.listarCobrancasCompetencia(competenciaSelecionada)
        );
    }
}
