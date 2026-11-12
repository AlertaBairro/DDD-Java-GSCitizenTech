package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.StatusOcorrencia;

import java.time.LocalDateTime;

/** Linha do historico de status (T_AB_HISTORICO_STATUS). */
public record HistoricoStatusResponse(
        Long id,
        Long ocorrenciaId,
        StatusOcorrencia statusAnterior,
        StatusOcorrencia statusNovo,
        ResumoResponse agente,
        LocalDateTime dataAlteracao,
        String observacao) {
}
