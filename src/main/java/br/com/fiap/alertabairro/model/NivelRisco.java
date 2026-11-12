package br.com.fiap.alertabairro.model;

/**
 * Nivel de risco calculado para uma ocorrencia.
 * ALTO: pontuacao >= 16 | MEDIO: pontuacao >= 10 | BAIXO: abaixo de 10.
 */
public enum NivelRisco {

    BAIXO("Baixo", "Risco baixo"),
    MEDIO("Médio", "Risco médio"),
    ALTO("Alto", "Risco alto");

    private final String descricao;
    private final String rotulo;

    NivelRisco(String descricao, String rotulo) {
        this.descricao = descricao;
        this.rotulo = rotulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getRotulo() {
        return rotulo;
    }
}
