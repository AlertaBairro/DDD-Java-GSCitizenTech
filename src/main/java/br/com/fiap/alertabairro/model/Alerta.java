package br.com.fiap.alertabairro.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Alerta de alagamento gerado a partir de um ponto alagado (tabela T_AB_ALERTA).
 */
public class Alerta {

    public static final int TAMANHO_MAXIMO_MENSAGEM = 300;

    /** id_alerta NUMBER(10) - PK (sequence SQ_AB_ALERTA) */
    private Long id;
    /** id_ponto_alagado NUMBER(10) - FK para T_AB_PONTO_ALAGADO */
    private Long idPontoAlagado;
    /** ds_mensagem VARCHAR2(300) */
    private String mensagem;
    /** nr_raio_metros NUMBER(5), maior que zero */
    private int raioMetros;
    /** dt_envio DATE */
    private LocalDateTime dataEnvio;

    public Alerta() {
    }

    public Alerta(Long id, Long idPontoAlagado, String mensagem, int raioMetros, LocalDateTime dataEnvio) {
        this.id = id;
        this.idPontoAlagado = idPontoAlagado;
        this.mensagem = mensagem;
        this.raioMetros = raioMetros;
        this.dataEnvio = dataEnvio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPontoAlagado() {
        return idPontoAlagado;
    }

    public void setIdPontoAlagado(Long idPontoAlagado) {
        this.idPontoAlagado = idPontoAlagado;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public int getRaioMetros() {
        return raioMetros;
    }

    public void setRaioMetros(int raioMetros) {
        this.raioMetros = raioMetros;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alerta outro)) return false;
        return id != null && Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Alerta{id=" + id + ", ponto=" + idPontoAlagado + ", raio=" + raioMetros + "m}";
    }
}
