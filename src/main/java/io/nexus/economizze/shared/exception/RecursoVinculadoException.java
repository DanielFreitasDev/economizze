package io.nexus.economizze.shared.exception;

/**
 * Excecao para bloqueio de exclusao quando existe relacionamento ativo.
 */
public class RecursoVinculadoException extends RuntimeException {

    public RecursoVinculadoException(String mensagem) {
        super(mensagem);
    }
}
