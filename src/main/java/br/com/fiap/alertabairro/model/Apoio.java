package br.com.fiap.alertabairro.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Apoio de um usuario a uma ocorrencia (tabela AB_APOIO).
 * Existe no maximo um apoio por usuario por ocorrencia.
 */
public class Apoio {

    private Long id;
    private Long usuarioId;
    private Long ocorrenciaId;
    private OffsetDateTime dataApoio;

    public Apoio() {
    }

    public Apoio(Long id, Long usuarioId, Long ocorrenciaId, OffsetDateTime dataApoio) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.ocorrenciaId = ocorrenciaId;
        this.dataApoio = dataApoio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getOcorrenciaId() {
        return ocorrenciaId;
    }

    public void setOcorrenciaId(Long ocorrenciaId) {
        this.ocorrenciaId = ocorrenciaId;
    }

    public OffsetDateTime getDataApoio() {
        return dataApoio;
    }

    public void setDataApoio(OffsetDateTime dataApoio) {
        this.dataApoio = dataApoio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Apoio outro)) return false;
        return Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Apoio{id=" + id + ", usuarioId=" + usuarioId + ", ocorrenciaId=" + ocorrenciaId + "}";
    }
}
