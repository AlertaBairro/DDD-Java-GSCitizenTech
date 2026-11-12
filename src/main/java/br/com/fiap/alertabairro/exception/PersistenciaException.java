package br.com.fiap.alertabairro.exception;

/**
 * Falha ao acessar o banco de dados (erro de conexao ou de SQL).
 * Lancada pela camada DAO e convertida em HTTP 500 pelo tratador global.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
