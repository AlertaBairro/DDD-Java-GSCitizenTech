package br.com.fiap.alertabairro.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Destinatario de um alerta (tabela T_AB_ALERTA_USUARIO).
 * Chave composta (id_alerta, id_usuario). A leitura e registrada em dt_leitura.
 */
public class AlertaUsuario {

    /** id_alerta NUMBER(10) - PK/FK para T_AB_ALERTA */
    private Long idAlerta;
    /** id_usuario NUMBER(10) - PK/FK para T_AB_USUARIO */
    private Long idUsuario;
    /** dt_recebimento DATE */
    private LocalDateTime dataRecebimento;
    /** dt_leitura DATE - nula enquanto nao lido */
    private LocalDateTime dataLeitura;

    public AlertaUsuario() {
    }

    public AlertaUsuario(Long idAlerta, Long idUsuario, LocalDateTime dataRecebimento, LocalDateTime dataLeitura) {
        this.idAlerta = idAlerta;
        this.idUsuario = idUsuario;
        this.dataRecebimento = dataRecebimento;
        this.dataLeitura = dataLeitura;
    }

    public boolean isLido() {
        return dataLeitura != null;
    }

    /** Marca como lido mantendo o CHECK: leitura nunca antes do recebimento. */
    public void marcarComoLido(LocalDateTime agora) {
        this.dataLeitura = (dataRecebimento != null && agora.isBefore(dataRecebimento)) ? dataRecebimento : agora;
    }

    public Long getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(Long idAlerta) {
        this.idAlerta = idAlerta;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(LocalDateTime dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    public LocalDateTime getDataLeitura() {
        return dataLeitura;
    }

    public void setDataLeitura(LocalDateTime dataLeitura) {
        this.dataLeitura = dataLeitura;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertaUsuario outro)) return false;
        return Objects.equals(idAlerta, outro.idAlerta) && Objects.equals(idUsuario, outro.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAlerta, idUsuario);
    }

    @Override
    public String toString() {
        return "AlertaUsuario{alerta=" + idAlerta + ", usuario=" + idUsuario + ", lido=" + isLido() + "}";
    }
}
