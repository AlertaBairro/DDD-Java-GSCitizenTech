package br.com.fiap.alertabairro.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro de uma mudanca de status feita por um agente
 * (tabela T_AB_HISTORICO_STATUS). O CHECK CK_AB_HIST_DIFERENTE exige
 * status anterior diferente do novo.
 */
public class HistoricoStatus {

    public static final int TAMANHO_MAXIMO_OBSERVACAO = 300;

    /** id_historico NUMBER(10) - PK (sequence SQ_AB_HISTORICO_STATUS) */
    private Long id;
    /** id_ocorrencia NUMBER(10) - FK para T_AB_OCORRENCIA */
    private Long idOcorrencia;
    /** id_usuario NUMBER(10) - FK para T_AB_USUARIO (agente que alterou) */
    private Long idUsuario;
    /** st_anterior VARCHAR2(15) */
    private StatusOcorrencia statusAnterior;
    /** st_novo VARCHAR2(15) */
    private StatusOcorrencia statusNovo;
    /** dt_alteracao DATE */
    private LocalDateTime dataAlteracao;
    /** ds_observacao VARCHAR2(300) - opcional */
    private String observacao;

    public HistoricoStatus() {
    }

    public HistoricoStatus(Long id, Long idOcorrencia, Long idUsuario, StatusOcorrencia statusAnterior,
                           StatusOcorrencia statusNovo, LocalDateTime dataAlteracao, String observacao) {
        this.id = id;
        this.idOcorrencia = idOcorrencia;
        this.idUsuario = idUsuario;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.dataAlteracao = dataAlteracao;
        this.observacao = observacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public StatusOcorrencia getStatusAnterior() {
        return statusAnterior;
    }

    public void setStatusAnterior(StatusOcorrencia statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    public StatusOcorrencia getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(StatusOcorrencia statusNovo) {
        this.statusNovo = statusNovo;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(LocalDateTime dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoricoStatus outro)) return false;
        return id != null && Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HistoricoStatus{id=" + id + ", ocorrencia=" + idOcorrencia + ", " + statusAnterior
                + " -> " + statusNovo + "}";
    }
}
