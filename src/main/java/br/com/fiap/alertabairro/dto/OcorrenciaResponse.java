package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.AvaliacaoRisco;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.StatusOcorrencia;

import java.time.OffsetDateTime;

/**
 * Ocorrencia devolvida pela API.
 *
 * apoiadaPorMim: o usuario do header X-Usuario-Id ja apoiou a ocorrencia
 *                (o front desabilita o botao "Apoiar").
 * minhaOcorrencia: o usuario do header foi quem registrou.
 * editavel: o usuario pode editar/excluir (e o autor e o status e RECEBIDA).
 */
public record OcorrenciaResponse(
        Long id,
        BairroResponse bairro,
        TipoProblemaResponse tipo,
        String observacoes,
        OffsetDateTime dataCriacao,
        int apoios,
        StatusOcorrencia status,
        String statusDescricao,
        OffsetDateTime dataResolucao,
        Double latitude,
        Double longitude,
        AutorResponse autor,
        boolean apoiadaPorMim,
        boolean minhaOcorrencia,
        boolean editavel,
        RiscoResponse risco) {

    public static OcorrenciaResponse de(Ocorrencia ocorrencia, AvaliacaoRisco risco,
                                        boolean apoiadaPorMim, Long usuarioId) {
        boolean minha = ocorrencia.foiRegistradaPor(usuarioId);
        return new OcorrenciaResponse(
                ocorrencia.getId(),
                BairroResponse.de(ocorrencia.getBairro()),
                TipoProblemaResponse.de(ocorrencia.getTipo()),
                ocorrencia.getObservacoes(),
                ocorrencia.getDataCriacao(),
                ocorrencia.getQuantidadeApoios(),
                ocorrencia.getStatus(),
                ocorrencia.getStatus().getDescricao(),
                ocorrencia.getDataResolucao(),
                ocorrencia.getLatitude(),
                ocorrencia.getLongitude(),
                AutorResponse.de(ocorrencia.getUsuario()),
                apoiadaPorMim,
                minha,
                minha && ocorrencia.isRecebida(),
                RiscoResponse.de(risco));
    }
}
