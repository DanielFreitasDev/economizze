package io.nexus.economizze.shared.exception;

/**
 * Excecao para violacoes de regras de negocio.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
