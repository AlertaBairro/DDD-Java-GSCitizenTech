package br.com.fiap.alertabairro.model;

/**
 * Zona da cidade a que o bairro pertence
 * (coluna T_AB_BAIRRO.nm_zona, CHECK CK_AB_BAIRRO_ZONA).
 */
public enum Zona {

    NORTE("Zona Norte"),
    SUL("Zona Sul"),
    LESTE("Zona Leste"),
    OESTE("Zona Oeste"),
    CENTRO("Centro");

    private final String descricao;

    Zona(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
