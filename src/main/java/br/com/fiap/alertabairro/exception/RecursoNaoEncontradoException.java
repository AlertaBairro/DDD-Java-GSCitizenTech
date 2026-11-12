package br.com.fiap.alertabairro.exception;

/**
 * Recurso (usuario, ocorrencia, bairro, tipo ou apoio) que nao existe. Resposta HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
