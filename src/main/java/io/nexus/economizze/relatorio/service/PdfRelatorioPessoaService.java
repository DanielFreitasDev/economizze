package io.nexus.economizze.relatorio.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import io.nexus.economizze.cobranca.dto.OcorrenciaCobrancaPessoaDto;
import io.nexus.economizze.cobranca.dto.RelatorioPessoaDto;
import io.nexus.economizze.cobranca.dto.ResumoCartaoPessoaDto;
import io.nexus.economizze.shared.util.FormatadorViewUtil;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsavel por gerar PDF analitico de pessoa por competencia.
 */
@Service
@RequiredArgsConstructor
public class PdfRelatorioPessoaService {

    private final FormatadorViewUtil formatador;

    /**
     * Gera bytes do PDF com resumo por cartao e detalhamento de cobrancas no mes.
     */
    public byte[] gerarPdf(RelatorioPessoaDto relatorio) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 32, 32, 32, 32);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            adicionarCabecalho(document, relatorio);
            adicionarResumoCartoes(document, relatorio);
            adicionarDetalhesCompetencia(document, relatorio);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Falha ao gerar PDF do relatorio", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Erro inesperado ao montar PDF", ex);
        }
    }

    /**
     * Adiciona cabecalho principal do relatorio.
     */
    private void adicionarCabecalho(Document document, RelatorioPessoaDto relatorio) throws DocumentException {
        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
        Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);

        document.add(new Paragraph("RELATORIO ANALITICO DE DIVIDA", titulo));
        document.add(new Paragraph("Pessoa: " + relatorio.pessoaNome(), subtitulo));
        document.add(new Paragraph("CPF: " + formatador.maskCpf(relatorio.cpf()), subtitulo));
        document.add(new Paragraph("Competencia: " + relatorio.competencia(), subtitulo));
        document.add(new Paragraph("Total geral em aberto: " + formatador.moeda(relatorio.totalGeralAberto()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        document.add(new Paragraph(" "));
    }

    /**
     * Adiciona tabela de resumo por cartao com totais por tipo.
     */
    private void adicionarResumoCartoes(Document document, RelatorioPessoaDto relatorio) throws DocumentException {
        document.add(new Paragraph("Resumo por cartao", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

        PdfPTable tabela = new PdfPTable(new float[]{2.6f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f});
        tabela.setWidthPercentage(100);

        adicionarCabecalhoTabela(tabela, "Cartao");
        adicionarCabecalhoTabela(tabela, "Avulso");
        adicionarCabecalhoTabela(tabela, "Recorrente");
        adicionarCabecalhoTabela(tabela, "Parcelado");
        adicionarCabecalhoTabela(tabela, "Encargos");
        adicionarCabecalhoTabela(tabela, "Pagamentos");
        adicionarCabecalhoTabela(tabela, "Saldo");

        for (ResumoCartaoPessoaDto resumo : relatorio.resumosPorCartao()) {
            tabela.addCell(celulaTexto(resumo.cartaoDescricao()));
            tabela.addCell(celulaTexto(formatador.moeda(resumo.totalAvulso())));
            tabela.addCell(celulaTexto(formatador.moeda(resumo.totalRecorrente())));
            tabela.addCell(celulaTexto(formatador.moeda(resumo.totalParcelado())));
            tabela.addCell(celulaTexto(formatador.moeda(resumo.totalEncargos())));
            tabela.addCell(celulaTexto(formatador.moeda(resumo.totalPagamentos())));
            tabela.addCell(celulaTexto(formatador.moeda(resumo.saldoAberto())));
        }

        document.add(tabela);
        document.add(new Paragraph(" "));
    }

    /**
     * Adiciona tabela detalhada com cobrancas da competencia selecionada.
     */
    private void adicionarDetalhesCompetencia(Document document, RelatorioPessoaDto relatorio) throws DocumentException {
        document.add(new Paragraph("Detalhamento da competencia", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

        PdfPTable tabela = new PdfPTable(new float[]{2.2f, 2.4f, 1.0f, 1.2f, 1.2f});
        tabela.setWidthPercentage(100);

        adicionarCabecalhoTabela(tabela, "Cartao");
        adicionarCabecalhoTabela(tabela, "Descricao");
        adicionarCabecalhoTabela(tabela, "Tipo");
        adicionarCabecalhoTabela(tabela, "Parcela");
        adicionarCabecalhoTabela(tabela, "Valor");

        for (OcorrenciaCobrancaPessoaDto cobranca : relatorio.cobrancasCompetencia()) {
            tabela.addCell(celulaTexto(cobranca.cartaoDescricao()));
            tabela.addCell(celulaTexto(cobranca.descricao()));
            tabela.addCell(celulaTexto(cobranca.tipo().name()));

            String parcela = cobranca.parcelaAtual() == null
                    ? "-"
                    : cobranca.parcelaAtual() + "/" + cobranca.totalParcelas();
            tabela.addCell(celulaTexto(parcela));
            tabela.addCell(celulaTexto(formatador.moeda(cobranca.valor())));
        }

        document.add(tabela);
    }

    /**
     * Adiciona cabecalho visual padrao para colunas das tabelas.
     */
    private void adicionarCabecalhoTabela(PdfPTable tabela, String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        celula.setBackgroundColor(new Color(30, 58, 138));
        celula.setPadding(6);
        tabela.addCell(celula);
    }

    /**
     * Cria celula de texto padrao para conteudo das tabelas.
     */
    private PdfPCell celulaTexto(String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto,
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK)));
        celula.setPadding(5);
        return celula;
    }
}
