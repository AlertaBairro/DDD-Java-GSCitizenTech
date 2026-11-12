package br.com.fiap.alertabairro.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registro ou edicao de ocorrencia. */
public record OcorrenciaRequest(
        @NotNull(message = "informe o tipo de ocorrência")
        Long tipoId,

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

        @Size(max = 500, message = "a descrição deve ter no máximo 500 caracteres")
        String descricao,

        @Size(max = 300, message = "a URL da foto deve ter no máximo 300 caracteres")
        @Pattern(regexp = "^https?://\\S+$", message = "a URL da foto deve começar com http:// ou https://")
        String urlFoto) {
}
