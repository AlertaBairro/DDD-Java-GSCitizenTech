package br.com.fiap.alertabairro.exception;

/**
 * Operacao que conflita com o estado atual, como apoio duplicado. Resposta HTTP 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
