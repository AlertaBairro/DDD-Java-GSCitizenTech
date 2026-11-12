package br.com.fiap.alertabairro.dto;

import java.util.List;

/**
 * Dados de referencia para montar os formularios e o mapa do front.
 */
public record ReferenciasResponse(
        List<BairroResponse> bairros,
        List<TipoProblemaResponse> tipos,
        List<OpcaoResponse> status,
        List<OpcaoResponse> perfis,
        PontoReferencia pontoReferencia,
        double raioZonaRiscoMetros) {

    /** Ponto de referencia do mapa (Estacao Jardim Sao Paulo-Ayrton Senna). */
    public record PontoReferencia(String nome, double latitude, double longitude) {
    }
}
