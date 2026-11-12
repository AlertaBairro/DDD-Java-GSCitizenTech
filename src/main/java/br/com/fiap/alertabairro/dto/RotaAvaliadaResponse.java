package br.com.fiap.alertabairro.dto;

import java.util.List;

/**
 * Resultado da avaliacao de uma rota candidata.
 *
 * @param indice              posicao da rota na lista enviada (comeca em 0)
 * @param zonasAtravessadas   quantidade de zonas de risco atravessadas
 * @param ocorrenciasNoCaminho ids das ocorrencias de alagamento no caminho
 * @param maisSegura          true para a rota recomendada
 */
public record RotaAvaliadaResponse(
        int indice,
        String nome,
        int zonasAtravessadas,
        List<Long> ocorrenciasNoCaminho,
        Double duracaoSegundos,
        Double distanciaMetros,
        boolean maisSegura) {
}
