package br.com.fiap.alertabairro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Rota candidata calculada pelo front (OSRM): lista de coordenadas [lat, lng],
 * duracao em segundos e distancia em metros.
 */
public record RotaCandidataRequest(
        @Schema(example = "Rota 1")
        String nome,

        @Schema(example = "[[-23.4930, -46.6180], [-23.4925, -46.6170], [-23.4920, -46.6160]]")
        @NotNull(message = "Informe as coordenadas da rota.")
        @Size(min = 2, message = "A rota deve ter ao menos 2 coordenadas.")
        List<List<Double>> coordenadas,

        @Schema(example = "180")
        @PositiveOrZero(message = "A duração não pode ser negativa.")
        Double duracaoSegundos,

        @Schema(example = "950")
        @PositiveOrZero(message = "A distância não pode ser negativa.")
        Double distanciaMetros) {
}
