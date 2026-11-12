package br.com.fiap.alertabairro.dto;

import java.time.LocalDateTime;

/** Alerta recebido pelo usuario (T_AB_ALERTA_USUARIO). */
public record MeuAlertaResponse(
        AlertaResponse alerta,
        LocalDateTime dataRecebimento,
        LocalDateTime dataLeitura,
        boolean lido) {
}
