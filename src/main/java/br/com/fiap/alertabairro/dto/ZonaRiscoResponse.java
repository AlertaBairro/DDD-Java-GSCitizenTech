package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Ocorrencia;

import java.time.OffsetDateTime;

/**
 * Zona de risco: ponto alagado ativo (com localizacao) e o raio de risco em metros.
 */
public record ZonaRiscoResponse(
        Long ocorrenciaId,
        String bairro,
        double latitude,
        double longitude,
        double raioMetros,
        int apoios,
        OffsetDateTime dataCriacao) {

    public static ZonaRiscoResponse de(Ocorrencia ocorrencia, double raioMetros) {
        return new ZonaRiscoResponse(ocorrencia.getId(), ocorrencia.getBairro().getNome(),
                ocorrencia.getLatitude(), ocorrencia.getLongitude(), raioMetros,
                ocorrencia.getQuantidadeApoios(), ocorrencia.getDataCriacao());
    }
}
