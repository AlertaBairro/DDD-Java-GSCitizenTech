package br.com.fiap.alertabairro.model;

import java.util.Objects;

/**
 * Bairro atendido pelo AlertaBairro (tabela T_AB_BAIRRO).
 */
public class Bairro {

    /** id_bairro NUMBER(5) - PK */
    private Long id;
    /** nm_bairro VARCHAR2(60) - UNIQUE */
    private String nome;
    /** nm_zona VARCHAR2(20) */
    private Zona zona;
    /** nm_subprefeitura VARCHAR2(60) */
    private String subprefeitura;

    public Bairro() {
    }

    public Bairro(Long id, String nome, Zona zona, String subprefeitura) {
        this.id = id;
        this.nome = nome;
        this.zona = zona;
        this.subprefeitura = subprefeitura;
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

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public String getSubprefeitura() {
        return subprefeitura;
    }

    public void setSubprefeitura(String subprefeitura) {
        this.subprefeitura = subprefeitura;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bairro outro)) return false;
        return Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Bairro{id=" + id + ", nome='" + nome + "', zona=" + zona + "}";
    }
}
