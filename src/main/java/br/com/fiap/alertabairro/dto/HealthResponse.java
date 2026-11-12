package br.com.fiap.alertabairro.dto;

import java.time.OffsetDateTime;

/**
 * Situacao da API e da conexao com o banco.
 *
 * @param status   "UP" quando a API esta no ar
 * @param banco    "UP" se o Oracle respondeu, "DOWN" caso contrario
 * @param mensagem detalhe para diagnostico
 */
public record HealthResponse(
        String status,
        String banco,
        String mensagem,
        String aplicacao,
        OffsetDateTime timestamp) {
}
