package br.com.fiap.alertabairro.model;

/**
 * Status de uma ocorrencia (coluna T_AB_OCORRENCIA.st_ocorrencia,
 * CHECK CK_AB_OCORRENCIA_STATUS). Toda ocorrencia nasce ABERTA.
 */
public enum StatusOcorrencia {

    ABERTA("Aberta"),
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
