package io.nexus.economizze.cobranca.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * DTO consolidado de relatorio de divida por pessoa.
 */
public record RelatorioPessoaDto(
        Long pessoaId,
        String pessoaNome,
        String cpf,
        YearMonth competencia,
        List<ResumoCartaoPessoaDto> resumosPorCartao,
        List<OcorrenciaCobrancaPessoaDto> cobrancasCompetencia,
        BigDecimal totalGeralAberto
) {
}
