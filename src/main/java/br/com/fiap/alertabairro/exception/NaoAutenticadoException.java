package br.com.fiap.alertabairro.exception;

/**
 * Usuario nao identificado ou credenciais invalidas. Resposta HTTP 401.
 */
public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
