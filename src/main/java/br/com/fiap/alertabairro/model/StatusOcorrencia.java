package br.com.fiap.alertabairro.model;

/**
 * Status de uma ocorrencia (coluna AB_OCORRENCIA.ST_OCORRENCIA).
 */
public enum StatusOcorrencia {

    RECEBIDA("Recebida"),
    EM_ATENDIMENTO("Em atendimento"),
    RESOLVIDA("Resolvida");

    private final String descricao;

    StatusOcorrencia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
