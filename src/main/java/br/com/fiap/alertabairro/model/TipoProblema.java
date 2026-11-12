package br.com.fiap.alertabairro.model;

import java.util.Objects;

/**
 * Tipo de problema de zeladoria (tabela AB_TIPO_PROBLEMA).
 * O peso entra no calculo de risco e o indicador de alagamento
 * identifica os pontos que geram alertas e zonas de risco.
 */
public class TipoProblema {

    public static final String ALAGADO = "ALAGADO";

    private String codigo;
    private String nome;
    private int peso;
    private boolean alagamento;

    public TipoProblema() {
    }

    public TipoProblema(String codigo, String nome, int peso, boolean alagamento) {
        this.codigo = codigo;
        this.nome = nome;
        this.peso = peso;
        this.alagamento = alagamento;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public boolean isAlagamento() {
        return alagamento;
    }

    public void setAlagamento(boolean alagamento) {
        this.alagamento = alagamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoProblema outro)) return false;
        return Objects.equals(codigo, outro.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "TipoProblema{codigo='" + codigo + "', nome='" + nome + "', peso=" + peso
                + ", alagamento=" + alagamento + "}";
    }
}
