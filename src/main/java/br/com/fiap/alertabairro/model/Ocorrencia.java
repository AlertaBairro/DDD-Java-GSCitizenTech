package br.com.fiap.alertabairro.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Ocorrencia de zeladoria registrada por um morador (tabela T_AB_OCORRENCIA).
 *
 * Regras do CHECK CK_AB_OCORRENCIA_RESOL: uma ocorrencia RESOLVIDA tem data
 * de resolucao (maior ou igual a de registro); qualquer outro status tem a
 * data de resolucao nula. O metodo {@link #alterarStatus} mantem isso.
 */
public class Ocorrencia {

    public static final int TAMANHO_MAXIMO_DESCRICAO = 500;
    public static final int TAMANHO_MAXIMO_ENDERECO = 150;
    public static final int TAMANHO_MAXIMO_URL_FOTO = 300;

    /** id_ocorrencia NUMBER(10) - PK (sequence SQ_AB_OCORRENCIA) */
    private Long id;
    /** id_usuario NUMBER(10) - FK para T_AB_USUARIO (autor) */
    private Long idUsuario;
    /** id_tipo_ocorrencia NUMBER(3) - FK para T_AB_TIPO_OCORRENCIA */
    private Long idTipoOcorrencia;
    /** id_bairro NUMBER(5) - FK para T_AB_BAIRRO */
    private Long idBairro;
    /** ds_ocorrencia VARCHAR2(500) - opcional */
    private String descricao;
    /** ds_endereco_ref VARCHAR2(150) */
    private String enderecoReferencia;
    /** nr_latitude NUMBER(9,6) */
    private double latitude;
    /** nr_longitude NUMBER(9,6) */
    private double longitude;
    /** ds_url_foto VARCHAR2(300) - opcional */
    private String urlFoto;
    /** dt_registro DATE */
    private LocalDateTime dataRegistro;
    /** nr_nivel_risco NUMBER(1), de 1 a 5 */
    private int nivelRisco;
    /** st_ocorrencia VARCHAR2(15) */
    private StatusOcorrencia status = StatusOcorrencia.ABERTA;
    /** dt_resolucao DATE - preenchida somente quando RESOLVIDA */
    private LocalDateTime dataResolucao;

    /** Quantidade de apoios, contada em T_AB_APOIO (valor derivado, nao e coluna). */
    private int quantidadeApoios;

    public Ocorrencia() {
    }

    public boolean isAberta() {
        return status == StatusOcorrencia.ABERTA;
    }

    public boolean isResolvida() {
        return status == StatusOcorrencia.RESOLVIDA;
    }

    public boolean foiRegistradaPor(Long idUsuario) {
        return this.idUsuario != null && this.idUsuario.equals(idUsuario);
    }

    /**
     * Muda o status mantendo a coerencia com a data de resolucao:
     * ao ir para RESOLVIDA preenche a data; ao sair de RESOLVIDA, limpa.
     */
    public void alterarStatus(StatusOcorrencia novoStatus, LocalDateTime agora) {
        this.status = novoStatus;
        if (novoStatus == StatusOcorrencia.RESOLVIDA) {
            this.dataResolucao = (dataRegistro != null && agora.isBefore(dataRegistro)) ? dataRegistro : agora;
        } else {
            this.dataResolucao = null;
        }
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

    public Long getIdTipoOcorrencia() {
        return idTipoOcorrencia;
    }

    public void setIdTipoOcorrencia(Long idTipoOcorrencia) {
        this.idTipoOcorrencia = idTipoOcorrencia;
    }

    public Long getIdBairro() {
        return idBairro;
    }

    public void setIdBairro(Long idBairro) {
        this.idBairro = idBairro;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public int getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(int nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public StatusOcorrencia getStatus() {
        return status;
    }

    public void setStatus(StatusOcorrencia status) {
        this.status = status;
    }

    public LocalDateTime getDataResolucao() {
        return dataResolucao;
    }

    public void setDataResolucao(LocalDateTime dataResolucao) {
        this.dataResolucao = dataResolucao;
    }

    public int getQuantidadeApoios() {
        return quantidadeApoios;
    }

    public void setQuantidadeApoios(int quantidadeApoios) {
        this.quantidadeApoios = quantidadeApoios;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ocorrencia outra)) return false;
        return id != null && Objects.equals(id, outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ocorrencia{id=" + id + ", tipo=" + idTipoOcorrencia + ", bairro=" + idBairro
                + ", status=" + status + ", nivelRisco=" + nivelRisco + "}";
    }
}
