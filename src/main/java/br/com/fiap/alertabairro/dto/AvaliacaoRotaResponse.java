package br.com.fiap.alertabairro.dto;

import java.util.List;

/**
 * Resultado da avaliacao de rotas, ja gravada em T_AB_CONSULTA_ROTA.
 *
 * @param consultaId        id da consulta gravada
 * @param zonasDeRisco      quantidade de pontos alagados ativos considerados
 * @param indiceMaisSegura  indice da rota recomendada (null se nenhuma rota foi enviada)
 * @param pontosEvitados    zonas da rota padrao menos zonas da rota segura (minimo 0)
 */
public record AvaliacaoRotaResponse(
        Long consultaId,
        int zonasDeRisco,
        int raioZonaMetros,
        boolean rotaCalculada,
        Integer indiceMaisSegura,
        int pontosEvitados,
        List<RotaAvaliadaResponse> rotas) {
}
