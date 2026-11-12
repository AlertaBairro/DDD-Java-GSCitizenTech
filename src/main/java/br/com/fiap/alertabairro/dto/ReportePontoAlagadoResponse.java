package br.com.fiap.alertabairro.dto;

/**
 * Resultado do reporte: o ponto criado, o alerta gerado e quantos usuarios
 * do bairro foram notificados.
 */
public record ReportePontoAlagadoResponse(
        PontoAlagadoResponse ponto,
        AlertaResponse alerta,
        int usuariosNotificados) {
}
