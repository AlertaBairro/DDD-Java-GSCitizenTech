package br.com.fiap.alertabairro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados para registrar (POST) ou editar (PUT) uma ocorrencia.
 * Latitude e longitude sao opcionais, mas devem ser informadas juntas.
 */
public record OcorrenciaRequest(
        @Schema(example = "2", description = "Id do bairro (veja GET /api/referencias)")
        @NotNull(message = "Informe o bairro.")
        Long bairroId,

        @Schema(example = "ALAGADO", description = "Código do tipo: ALAGADO, BUEIRO, BOCA ou LIXO")
        @NotBlank(message = "Informe o tipo de problema.")
        String tipo,

        @Schema(example = "Água acumulada na esquina da estação.")
        @Size(max = 400, message = "As observações devem ter no máximo 400 caracteres.")
        String observacoes,

        @Schema(example = "-23.4927")
        @DecimalMin(value = "-90", message = "A latitude deve estar entre -90 e 90.")
        @DecimalMax(value = "90", message = "A latitude deve estar entre -90 e 90.")
        Double latitude,

        @Schema(example = "-46.6168")
        @DecimalMin(value = "-180", message = "A longitude deve estar entre -180 e 180.")
        @DecimalMax(value = "180", message = "A longitude deve estar entre -180 e 180.")
        Double longitude) {
}
