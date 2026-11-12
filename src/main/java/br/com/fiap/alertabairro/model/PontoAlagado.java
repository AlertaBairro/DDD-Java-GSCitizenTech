package br.com.fiap.alertabairro.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Ponto de alagamento reportado por um morador (tabela T_AB_PONTO_ALAGADO).
 *
 * CHECK CK_AB_PONTO_ATIVO: ativo ('S') sem data de encerramento, ou
 * encerrado ('N') com data de encerramento maior ou igual a do reporte.
 */
public class PontoAlagado {

    /** id_ponto_alagado NUMBER(10) - PK (sequence SQ_AB_PONTO_ALAGADO) */
    private Long id;
    /** id_usuario NUMBER(10) - FK para T_AB_USUARIO (quem reportou) */
    private Long idUsuario;
    /** id_bairro NUMBER(5) - FK para T_AB_BAIRRO */
    private Long idBairro;
    /** ds_endereco_ref VARCHAR2(150) */
    private String enderecoReferencia;
    /** nr_latitude NUMBER(9,6) */
    private double latitude;
    /** nr_longitude NUMBER(9,6) */
    private double longitude;
    /** tp_nivel_agua VARCHAR2(15) */
    private NivelAgua nivelAgua;
    /** dt_reporte DATE */
    private LocalDateTime dataReporte;
    /** st_ativo CHAR(1): 'S' = true, 'N' = false */
    private boolean ativo = true;
    /** dt_encerramento DATE - preenchida quando encerrado */
    private LocalDateTime dataEncerramento;

    public PontoAlagado() {
    }

    /** Encerra o ponto mantendo o CHECK: encerramento nunca antes do reporte. */
    public void encerrar(LocalDateTime agora) {
        this.ativo = false;
        this.dataEncerramento = (dataReporte != null && agora.isBefore(dataReporte)) ? dataReporte : agora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdBairro() {
        return idBairro;
    }

    public void setIdBairro(Long idBairro) {
        this.idBairro = idBairro;
    }

    public String getEnderecoReferencia() {
        return enderecoReferencia;
    }

    public void setEnderecoReferencia(String enderecoReferencia) {
        this.enderecoReferencia = enderecoReferencia;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public NivelAgua getNivelAgua() {
        return nivelAgua;
    }

    public void setNivelAgua(NivelAgua nivelAgua) {
        this.nivelAgua = nivelAgua;
    }

    public LocalDateTime getDataReporte() {
        return dataReporte;
    }

    public void setDataReporte(LocalDateTime dataReporte) {
        this.dataReporte = dataReporte;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDateTime dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PontoAlagado outro)) return false;
        return id != null && Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PontoAlagado{id=" + id + ", endereco='" + enderecoReferencia + "', nivel=" + nivelAgua
                + ", ativo=" + ativo + "}";
    }
}
