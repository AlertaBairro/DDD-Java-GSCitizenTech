package br.com.fiap.alertabairro.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Usuario do AlertaBairro (tabela T_AB_USUARIO).
 * A senha nunca e guardada em texto puro: apenas o hash BCrypt.
 * A exclusao e logica: st_ativo passa de 'S' para 'N'.
 */
public class Usuario {

    /** id_usuario NUMBER(10) - PK (sequence SQ_AB_USUARIO) */
    private Long id;
    /** id_bairro NUMBER(5) - FK para T_AB_BAIRRO, obrigatorio */
    private Long idBairro;
    /** nm_usuario VARCHAR2(100) */
    private String nome;
    /** ds_email VARCHAR2(120) - UNIQUE */
    private String email;
    /** ds_senha_hash VARCHAR2(255) */
    private String senhaHash;
    /** nr_telefone VARCHAR2(15) - opcional */
    private String telefone;
    /** tp_perfil VARCHAR2(10) */
    private Perfil perfil;
    /** dt_cadastro DATE */
    private LocalDateTime dataCadastro;
    /** st_ativo CHAR(1): 'S' = true, 'N' = false */
    private boolean ativo = true;

    public Usuario() {
    }

    public Usuario(Long id, Long idBairro, String nome, String email, String senhaHash, String telefone,
                   Perfil perfil, LocalDateTime dataCadastro, boolean ativo) {
        this.id = id;
        this.idBairro = idBairro;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.telefone = telefone;
        this.perfil = perfil;
        this.dataCadastro = dataCadastro;
        this.ativo = ativo;
    }

    public boolean temPerfil(Perfil perfil) {
        return this.perfil == perfil;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdBairro() {
        return idBairro;
    }

    public void setIdBairro(Long idBairro) {
        this.idBairro = idBairro;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario outro)) return false;
        return Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nome='" + nome + "', email='" + email + "', perfil=" + perfil
                + ", ativo=" + ativo + "}";
    }
}
