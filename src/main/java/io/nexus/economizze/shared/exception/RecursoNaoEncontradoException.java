package io.nexus.economizze.shared.exception;

/**
 * Excecao para cenarios em que um recurso obrigatorio nao foi encontrado.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
