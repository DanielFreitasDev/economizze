package io.nexus.economizze.dashboard.controller;

import io.nexus.economizze.cobranca.service.CobrancaPessoaService;
import io.nexus.economizze.dashboard.service.DashboardService;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller da tela inicial com consolidacao mensal de fluxo e cobrancas.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CobrancaPessoaService cobrancaPessoaService;

    /**
     * Exibe dashboard com filtros por competencia.
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "competencia", required = false) YearMonth competencia,
            Model model
    ) {
        YearMonth competenciaSelecionada = competencia == null ? YearMonth.now() : competencia;

        model.addAttribute("competencia", competenciaSelecionada);
        model.addAttribute("resumo", dashboardService.calcularResumo(competenciaSelecionada));
        model.addAttribute("lancamentos", dashboardService.listarLancamentosCompetencia(competenciaSelecionada));
        model.addAttribute("transferencias", dashboardService.listarTransferenciasCompetencia(competenciaSelecionada));
        model.addAttribute("cobrancas", cobrancaPessoaService.listarOcorrenciasDaCompetencia(competenciaSelecionada));

        return "dashboard";
    }
}
