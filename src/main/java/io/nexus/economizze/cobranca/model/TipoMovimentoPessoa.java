package io.nexus.economizze.cobranca.model;

/**
 * Tipos de movimento no extrato da pessoa (pagamentos e encargos).
 */
public enum TipoMovimentoPessoa {
    PAGAMENTO,
    ENCARGO_JUROS,
    ENCARGO_MULTA,
    AJUSTE_CREDITO,
    AJUSTE_DEBITO
}
