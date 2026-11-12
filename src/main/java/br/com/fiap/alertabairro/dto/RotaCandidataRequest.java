package br.com.fiap.alertabairro.dto;

import java.util.List;

/**
 * Rota candidata calculada no front (OSRM).
 *
 * @param coordenadas     lista de pontos [lat, lng] da polilinha
 * @param duracaoSegundos usada no desempate da rota mais segura
 */
public record RotaCandidataRequest(
        String nome,
        List<List<Double>> coordenadas,
        Double duracaoSegundos,
        Double distanciaMetros) {
}
