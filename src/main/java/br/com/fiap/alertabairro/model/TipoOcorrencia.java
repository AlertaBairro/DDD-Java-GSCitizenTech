package br.com.fiap.alertabairro.model;

import java.util.Objects;

/**
 * Tipo de problema de zeladoria (tabela T_AB_TIPO_OCORRENCIA).
 * Lido do banco; o peso de risco (1 a 5) vira o nivel de risco inicial
 * da ocorrencia registrada com esse tipo.
 */
public class TipoOcorrencia {

    public static final int PESO_MINIMO = 1;
    public static final int PESO_MAXIMO = 5;

    /** id_tipo_ocorrencia NUMBER(3) - PK */
    private Long id;
    /** nm_tipo VARCHAR2(50) - UNIQUE */
    private String nome;
    /** ds_tipo VARCHAR2(200) - opcional */
    private String descricao;
    /** nr_peso_risco NUMBER(1), de 1 a 5 */
    private int pesoRisco;

    public TipoOcorrencia() {
    }

    public TipoOcorrencia(Long id, String nome, String descricao, int pesoRisco) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.pesoRisco = pesoRisco;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getPesoRisco() {
        return pesoRisco;
    }

    public void setPesoRisco(int pesoRisco) {
        this.pesoRisco = pesoRisco;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoOcorrencia outro)) return false;
        return Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TipoOcorrencia{id=" + id + ", nome='" + nome + "', pesoRisco=" + pesoRisco + "}";
    }
}
