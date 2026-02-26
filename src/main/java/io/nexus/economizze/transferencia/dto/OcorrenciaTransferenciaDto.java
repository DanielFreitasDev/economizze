package io.nexus.economizze.transferencia.dto;

import io.nexus.economizze.lancamento.model.TipoLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de ocorrencia mensal derivada de uma transferencia base.
 */
public record OcorrenciaTransferenciaDto(
        Long transferenciaId,
        String descricao,
        TipoLancamento tipo,
        BigDecimal valor,
        LocalDate dataOcorrencia,
        Integer parcelaAtual,
        Integer totalParcelas,
        String contaOrigem,
        String contaDestino
) {
}
