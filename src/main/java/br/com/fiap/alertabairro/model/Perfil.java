package br.com.fiap.alertabairro.model;

/**
 * Perfis de usuario do AlertaBairro
 * (coluna T_AB_USUARIO.tp_perfil, CHECK CK_AB_USUARIO_PERFIL).
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
