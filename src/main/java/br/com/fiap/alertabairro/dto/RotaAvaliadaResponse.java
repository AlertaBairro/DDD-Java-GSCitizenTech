package br.com.fiap.alertabairro.dto;

import java.util.List;

/**
 * Resultado de uma rota candidata.
 *
 * @param zonasAtravessadas       quantidade de zonas de risco a ate 40 m da rota
 * @param pontosAlagadosNoCaminho IDs dos pontos alagados atravessados
 */
public record RotaAvaliadaResponse(
        int indice,
        String nome,
        int zonasAtravessadas,
        List<Long> pontosAlagadosNoCaminho,
        Double duracaoSegundos,
        Double distanciaMetros,
        boolean maisSegura) {
}
