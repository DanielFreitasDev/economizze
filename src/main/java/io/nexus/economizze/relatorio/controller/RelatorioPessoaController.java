package io.nexus.economizze.relatorio.controller;

import io.nexus.economizze.relatorio.dto.FiltroRelatorioPessoaForm;
import io.nexus.economizze.relatorio.service.PdfRelatorioPessoaService;
import io.nexus.economizze.relatorio.service.RelatorioPessoaService;
import io.nexus.economizze.pessoa.service.PessoaService;
import jakarta.validation.Valid;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller de relatorios analiticos de pessoas.
 */
@Controller
@RequestMapping("/relatorios/pessoas")
@RequiredArgsConstructor
public class RelatorioPessoaController {

    private final PessoaService pessoaService;
    private final RelatorioPessoaService relatorioPessoaService;
    private final PdfRelatorioPessoaService pdfRelatorioPessoaService;

    /**
     * Exibe tela de filtro e consulta do relatorio.
     */
    @GetMapping
    public String exibir(
            @ModelAttribute("filtro") FiltroRelatorioPessoaForm filtro,
            Model model
    ) {
        if (filtro.getCompetencia() == null) {
            filtro.setCompetencia(YearMonth.now());
        }

        model.addAttribute("pessoas", pessoaService.listar(false));
        model.addAttribute("resumoGeral", relatorioPessoaService.listarResumoGeral(filtro.getCompetencia()));

        if (filtro.getPessoaId() != null && filtro.getCompetencia() != null) {
            model.addAttribute("relatorio", relatorioPessoaService.gerarRelatorio(filtro.getPessoaId(), filtro.getCompetencia()));
        }

        return "relatorios/pessoas";
    }

    /**
     * Gera PDF analitico da pessoa/competencia selecionada.
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> gerarPdf(
            @Valid FiltroRelatorioPessoaForm filtro,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("Filtros invalidos para geracao do PDF");
        }

        var relatorio = relatorioPessoaService.gerarRelatorio(filtro.getPessoaId(), filtro.getCompetencia());
        byte[] pdf = pdfRelatorioPessoaService.gerarPdf(relatorio);

        String nomeArquivo = "relatorio_pessoa_" + filtro.getPessoaId() + "_" + filtro.getCompetencia() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(nomeArquivo).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
