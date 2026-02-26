package io.nexus.economizze.relatorio.api;

import io.nexus.economizze.cobranca.dto.RelatorioPessoaDto;
import io.nexus.economizze.relatorio.service.RelatorioPessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de relatorios analiticos de pessoa.
 */
@RestController
@RequestMapping("/api/relatorios/pessoas")
@RequiredArgsConstructor
@Tag(name = "Relatorios")
public class RelatorioPessoaApiController {

    private final RelatorioPessoaService relatorioPessoaService;

    /**
     * Retorna consolidado analitico de pessoa por competencia.
     */
    @Operation(summary = "Gerar relatorio analitico de pessoa")
    @GetMapping("/{pessoaId}")
    public RelatorioPessoaDto gerar(
            @PathVariable Long pessoaId,
            @RequestParam(value = "competencia", required = false) YearMonth competencia
    ) {
        YearMonth competenciaSelecionada = competencia == null ? YearMonth.now() : competencia;
        return relatorioPessoaService.gerarRelatorio(pessoaId, competenciaSelecionada);
    }
}
