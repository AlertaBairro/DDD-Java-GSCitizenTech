package br.com.fiap.alertabairro.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Apoio ("eu tambem vi") de um morador a uma ocorrencia (tabela T_AB_APOIO).
 * Chave composta (id_ocorrencia, id_usuario): um apoio por usuario.
 */
public class Apoio {

    /** id_ocorrencia NUMBER(10) - PK/FK para T_AB_OCORRENCIA */
    private Long idOcorrencia;
    /** id_usuario NUMBER(10) - PK/FK para T_AB_USUARIO */
    private Long idUsuario;
    /** dt_apoio DATE */
    private LocalDateTime dataApoio;

    public Apoio() {
    }

    public Apoio(Long idOcorrencia, Long idUsuario, LocalDateTime dataApoio) {
        this.idOcorrencia = idOcorrencia;
        this.idUsuario = idUsuario;
        this.dataApoio = dataApoio;
    }

    public Long getIdOcorrencia() {
        return idOcorrencia;
    }

    public void setIdOcorrencia(Long idOcorrencia) {
        this.idOcorrencia = idOcorrencia;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getDataApoio() {
        return dataApoio;
    }

    public void setDataApoio(LocalDateTime dataApoio) {
        this.dataApoio = dataApoio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Apoio outro)) return false;
        return Objects.equals(idOcorrencia, outro.idOcorrencia) && Objects.equals(idUsuario, outro.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOcorrencia, idUsuario);
    }

    @Override
    public String toString() {
        return "Apoio{ocorrencia=" + idOcorrencia + ", usuario=" + idUsuario + "}";
    }
}
