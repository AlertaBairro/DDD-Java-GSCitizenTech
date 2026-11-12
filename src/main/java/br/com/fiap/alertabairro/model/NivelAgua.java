package br.com.fiap.alertabairro.model;

/**
 * Nivel da agua em um ponto alagado (coluna T_AB_PONTO_ALAGADO.tp_nivel_agua,
 * CHECK CK_AB_PONTO_NIVEL). Cada nivel define o raio, em metros, em que o
 * alerta gerado pelo ponto e divulgado (T_AB_ALERTA.nr_raio_metros).
 */
public enum NivelAgua {

    TORNOZELO("na altura do tornozelo", 300),
    JOELHO("na altura do joelho", 500),
    CINTURA("na altura da cintura", 600),
    INTRANSITAVEL("intransitável", 800);

    private final String descricao;
    private final int raioAlertaMetros;

    NivelAgua(String descricao, int raioAlertaMetros) {
        this.descricao = descricao;
        this.raioAlertaMetros = raioAlertaMetros;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getRaioAlertaMetros() {
        return raioAlertaMetros;
    }
}
