package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.StatusOcorrencia;

import java.time.LocalDateTime;

/**
 * Ocorrencia com os dados de exibicao.
 *
 * @param apoiadaPorMim   o usuario do header ja apoiou
 * @param minhaOcorrencia o usuario do header e o autor
 * @param editavel        o usuario do header pode editar/excluir (autor e status ABERTA)
 * @param prioridade      pontuacao e nivel calculados para a fila do agente
 */
public record OcorrenciaResponse(
        Long id,
        ResumoResponse tipo,
        ResumoResponse bairro,
        ResumoResponse autor,
        String descricao,
        String enderecoReferencia,
        double latitude,
        double longitude,
        String urlFoto,
        LocalDateTime dataRegistro,
        int nivelRisco,
        StatusOcorrencia status,
        String statusDescricao,
        LocalDateTime dataResolucao,
        int quantidadeApoios,
        boolean apoiadaPorMim,
        boolean minhaOcorrencia,
        boolean editavel,
        RiscoResponse prioridade) {
}
