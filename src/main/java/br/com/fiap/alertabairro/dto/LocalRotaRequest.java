package br.com.fiap.alertabairro.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Origem ou destino de uma consulta de rota (descricao + coordenadas). */
public record LocalRotaRequest(
        @NotBlank(message = "informe a descrição do local")
        @Size(max = 150, message = "a descrição do local deve ter no máximo 150 caracteres")
        String descricao,

        @NotNull(message = "informe a latitude")
        @DecimalMin(value = "-90", message = "latitude deve estar entre -90 e 90")
        @DecimalMax(value = "90", message = "latitude deve estar entre -90 e 90")
        Double latitude,

        @NotNull(message = "informe a longitude")
        @DecimalMin(value = "-180", message = "longitude deve estar entre -180 e 180")
        @DecimalMax(value = "180", message = "longitude deve estar entre -180 e 180")
        Double longitude) {
}
