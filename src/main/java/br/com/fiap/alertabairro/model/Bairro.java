package br.com.fiap.alertabairro.model;

import java.util.Objects;

/**
 * Bairro atendido pelo AlertaBairro (tabela AB_BAIRRO).
 */
public class Bairro {

    private Long id;
    private String nome;

    public Bairro() {
    }

    public Bairro(Long id, String nome) {
        this.id = id;
        this.nome = nome;
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
        return "Bairro{id=" + id + ", nome='" + nome + "'}";
    }
}
