package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.AvaliacaoRisco;
import br.com.fiap.alertabairro.model.NivelRisco;

/**
 * Risco calculado de uma ocorrencia: pontuacao (1 casa decimal), nivel e rotulo.
 */
public record RiscoResponse(double pontuacao, NivelRisco nivel, String nivelDescricao, String rotulo) {

    public static RiscoResponse de(AvaliacaoRisco avaliacao) {
        double arredondada = Math.round(avaliacao.getPontuacao() * 10.0) / 10.0;
        return new RiscoResponse(arredondada, avaliacao.getNivel(),
                avaliacao.getNivel().getDescricao(), avaliacao.getRotulo());
    }
}
