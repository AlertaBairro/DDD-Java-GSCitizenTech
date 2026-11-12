package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.StatusOcorrencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Mudanca de status pelo agente, com observacao opcional gravada no historico. */
public record StatusRequest(
        @NotNull(message = "informe o status (RECEBIDA, EM_ATENDIMENTO ou RESOLVIDA)")
        StatusOcorrencia status,

        @Size(max = 300, message = "a observação deve ter no máximo 300 caracteres")
        String observacao) {
}
