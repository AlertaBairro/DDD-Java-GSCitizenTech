package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.AvaliacaoRisco;
import br.com.fiap.alertabairro.model.NivelRisco;

/**
 * Prioridade calculada para o agente: pontuacao = nivelRisco * 2 + apoios + dias * 0.8.
 */
public record RiscoResponse(double pontuacao, NivelRisco nivel, String nivelDescricao, String rotulo) {

    public static RiscoResponse de(AvaliacaoRisco a) {
        double arredondada = Math.round(a.getPontuacao() * 10.0) / 10.0;
        return new RiscoResponse(arredondada, a.getNivel(), a.getNivel().getDescricao(), a.getRotulo());
    }
}
