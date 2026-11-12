package br.com.fiap.alertabairro.exception;

/**
 * Perfil sem permissao para a operacao ou usuario que nao e o dono do recurso. Resposta HTTP 403.
 */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
