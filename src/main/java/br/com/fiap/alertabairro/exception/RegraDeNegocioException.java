package br.com.fiap.alertabairro.exception;

/**
 * Violacao de regra de negocio, como editar uma ocorrencia que nao esta RECEBIDA ou apoiar uma ocorrencia RESOLVIDA. Resposta HTTP 422.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
