package br.com.fiap.alertabairro.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Corpo JSON padronizado devolvido em todas as respostas de erro da API.
 *
 * @param timestamp momento do erro
 * @param status    codigo HTTP
 * @param erro      descricao curta do codigo HTTP
 * @param mensagem  mensagem para o usuario, em portugues
 * @param caminho   rota que gerou o erro
 * @param detalhes  erros de validacao campo a campo (quando houver)
 */
public record ErroResposta(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<String> detalhes) {
}
