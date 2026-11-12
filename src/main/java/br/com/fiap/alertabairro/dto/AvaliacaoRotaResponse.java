package br.com.fiap.alertabairro.dto;

import java.util.List;

/**
 * Resultado da avaliacao das rotas: cada rota com as zonas atravessadas e o
 * indice da rota mais segura (menos zonas; no empate, menor duracao).
 */
public record AvaliacaoRotaResponse(
        int totalZonasDeRisco,
        double raioMetros,
        int indiceRotaMaisSegura,
        List<RotaAvaliadaResponse> rotas) {
}
