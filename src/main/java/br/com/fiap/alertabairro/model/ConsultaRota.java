package br.com.fiap.alertabairro.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Consulta de rota segura feita por um usuario (tabela T_AB_CONSULTA_ROTA).
 */
public class ConsultaRota {

    public static final int TAMANHO_MAXIMO_DESCRICAO = 150;

    /** id_consulta_rota NUMBER(10) - PK (sequence SQ_AB_CONSULTA_ROTA) */
    private Long id;
    /** id_usuario NUMBER(10) - FK para T_AB_USUARIO */
    private Long idUsuario;
    /** ds_origem VARCHAR2(150) */
    private String origem;
    /** nr_lat_origem NUMBER(9,6) */
    private double latitudeOrigem;
    /** nr_long_origem NUMBER(9,6) */
    private double longitudeOrigem;
    /** ds_destino VARCHAR2(150) */
    private String destino;
    /** nr_lat_destino NUMBER(9,6) */
    private double latitudeDestino;
    /** nr_long_destino NUMBER(9,6) */
    private double longitudeDestino;
    /** dt_consulta DATE */
    private LocalDateTime dataConsulta;
    /** st_rota_calculada CHAR(1): 'S' = true, 'N' = false */
    private boolean rotaCalculada;
    /** nr_pontos_evitados NUMBER(3), maior ou igual a zero */
    private int pontosEvitados;

    public ConsultaRota() {
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

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public double getLatitudeOrigem() {
        return latitudeOrigem;
    }

    public void setLatitudeOrigem(double latitudeOrigem) {
        this.latitudeOrigem = latitudeOrigem;
    }

    public double getLongitudeOrigem() {
        return longitudeOrigem;
    }

    public void setLongitudeOrigem(double longitudeOrigem) {
        this.longitudeOrigem = longitudeOrigem;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public double getLatitudeDestino() {
        return latitudeDestino;
    }

    public void setLatitudeDestino(double latitudeDestino) {
        this.latitudeDestino = latitudeDestino;
    }

    public double getLongitudeDestino() {
        return longitudeDestino;
    }

    public void setLongitudeDestino(double longitudeDestino) {
        this.longitudeDestino = longitudeDestino;
    }

    public LocalDateTime getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDateTime dataConsulta) {
        this.dataConsulta = dataConsulta;
    }

    public boolean isRotaCalculada() {
        return rotaCalculada;
    }

    public void setRotaCalculada(boolean rotaCalculada) {
        this.rotaCalculada = rotaCalculada;
    }

    public int getPontosEvitados() {
        return pontosEvitados;
    }

    public void setPontosEvitados(int pontosEvitados) {
        this.pontosEvitados = pontosEvitados;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultaRota outra)) return false;
        return id != null && Objects.equals(id, outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ConsultaRota{id=" + id + ", origem='" + origem + "', destino='" + destino
                + "', pontosEvitados=" + pontosEvitados + "}";
    }
}
