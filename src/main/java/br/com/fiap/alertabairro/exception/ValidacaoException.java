package br.com.fiap.alertabairro.exception;

/**
 * Dados de entrada invalidos (formato, faixa ou campo obrigatorio). Resposta HTTP 400.
 */
public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
