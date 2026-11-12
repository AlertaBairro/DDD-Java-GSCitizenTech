package br.com.fiap.alertabairro.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Ocorrencia de zeladoria registrada por um morador (tabela AB_OCORRENCIA).
 */
public class Ocorrencia {

    public static final int TAMANHO_MAXIMO_OBSERVACOES = 400;

    private Long id;
    private Bairro bairro;
    private TipoProblema tipo;
    private Usuario usuario;
    private String observacoes;
    private OffsetDateTime dataCriacao;
    private int quantidadeApoios;
    private StatusOcorrencia status;
    private OffsetDateTime dataResolucao;
    private Double latitude;
    private Double longitude;

    public Ocorrencia() {
    }

    public Ocorrencia(Long id, Bairro bairro, TipoProblema tipo, Usuario usuario, String observacoes,
                      OffsetDateTime dataCriacao, int quantidadeApoios, StatusOcorrencia status,
                      OffsetDateTime dataResolucao, Double latitude, Double longitude) {
        this.id = id;
        this.bairro = bairro;
        this.tipo = tipo;
        this.usuario = usuario;
        this.observacoes = observacoes;
        this.dataCriacao = dataCriacao;
        this.quantidadeApoios = quantidadeApoios;
        this.status = status;
        this.dataResolucao = dataResolucao;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isResolvida() {
        return status == StatusOcorrencia.RESOLVIDA;
    }

    public boolean isRecebida() {
        return status == StatusOcorrencia.RECEBIDA;
    }

    public boolean possuiLocalizacao() {
        return latitude != null && longitude != null;
    }

    public boolean isAlagamento() {
        return tipo != null && (tipo.isAlagamento() || TipoProblema.ALAGADO.equals(tipo.getCodigo()));
    }

    public boolean foiRegistradaPor(Long usuarioId) {
        return usuario != null && Objects.equals(usuario.getId(), usuarioId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bairro getBairro() {
        return bairro;
    }

    public void setBairro(Bairro bairro) {
        this.bairro = bairro;
    }

    public TipoProblema getTipo() {
        return tipo;
    }

    public void setTipo(TipoProblema tipo) {
        this.tipo = tipo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public OffsetDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(OffsetDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public int getQuantidadeApoios() {
        return quantidadeApoios;
    }

    public void setQuantidadeApoios(int quantidadeApoios) {
        this.quantidadeApoios = quantidadeApoios;
    }

    public StatusOcorrencia getStatus() {
        return status;
    }

    public void setStatus(StatusOcorrencia status) {
        this.status = status;
    }

    public OffsetDateTime getDataResolucao() {
        return dataResolucao;
    }

    public void setDataResolucao(OffsetDateTime dataResolucao) {
        this.dataResolucao = dataResolucao;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ocorrencia outra)) return false;
        return Objects.equals(id, outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ocorrencia{id=" + id + ", bairro=" + (bairro != null ? bairro.getNome() : null)
                + ", tipo=" + (tipo != null ? tipo.getCodigo() : null) + ", status=" + status
                + ", apoios=" + quantidadeApoios + "}";
    }
}
