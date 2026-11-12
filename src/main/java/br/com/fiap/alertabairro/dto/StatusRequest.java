package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.StatusOcorrencia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Novo status de uma ocorrencia (alterado pelo agente de zeladoria).
 */
public record StatusRequest(
        @Schema(example = "EM_ATENDIMENTO")
        @NotNull(message = "Informe o status (RECEBIDA, EM_ATENDIMENTO ou RESOLVIDA).")
        StatusOcorrencia status) {
}
