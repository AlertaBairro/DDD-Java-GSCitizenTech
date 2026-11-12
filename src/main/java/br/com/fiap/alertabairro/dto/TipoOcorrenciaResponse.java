package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.TipoOcorrencia;

/** Tipo de ocorrencia (T_AB_TIPO_OCORRENCIA). */
public record TipoOcorrenciaResponse(Long id, String nome, String descricao, int pesoRisco) {

    public static TipoOcorrenciaResponse de(TipoOcorrencia t) {
        return new TipoOcorrenciaResponse(t.getId(), t.getNome(), t.getDescricao(), t.getPesoRisco());
    }
}
