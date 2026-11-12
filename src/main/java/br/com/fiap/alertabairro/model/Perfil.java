package br.com.fiap.alertabairro.model;

/**
 * Perfis de usuario do AlertaBairro (coluna AB_USUARIO.TP_PERFIL).
 */
public enum Perfil {

    MORADOR("Morador"),
    AGENTE("Agente de zeladoria"),
    GESTOR("Gestor da subprefeitura");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
