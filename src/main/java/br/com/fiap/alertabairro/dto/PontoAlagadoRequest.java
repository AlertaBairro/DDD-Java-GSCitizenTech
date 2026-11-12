package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.NivelAgua;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Reporte de ponto alagado pelo morador. */
public record PontoAlagadoRequest(
        @NotNull(message = "informe o bairro")
        Long bairroId,

        @NotBlank(message = "informe o endereço de referência")
        @Size(max = 150, message = "o endereço de referência deve ter no máximo 150 caracteres")
        String enderecoReferencia,

        @NotNull(message = "informe a latitude")
        @DecimalMin(value = "-90", message = "latitude deve estar entre -90 e 90")
        @DecimalMax(value = "90", message = "latitude deve estar entre -90 e 90")
        Double latitude,

        @NotNull(message = "informe a longitude")
        @DecimalMin(value = "-180", message = "longitude deve estar entre -180 e 180")
        @DecimalMax(value = "180", message = "longitude deve estar entre -180 e 180")
        Double longitude,

        @NotNull(message = "informe o nível da água (TORNOZELO, JOELHO, CINTURA ou INTRANSITAVEL)")
        NivelAgua nivelAgua) {
}
