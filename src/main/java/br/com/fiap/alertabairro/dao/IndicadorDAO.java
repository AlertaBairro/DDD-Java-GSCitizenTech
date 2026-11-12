package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas agregadas para o painel do gestor (somente leitura).
 * O indicador por bairro reproduz a consulta 2 do DQL do grupo
 * (INNER JOIN + GROUP BY em T_AB_OCORRENCIA e T_AB_BAIRRO).
 */
@Repository
public class IndicadorDAO {

    /** Totais de ocorrencias; mediaHorasResolucao e nula se nenhuma foi resolvida. */
    public record TotaisOcorrencias(long total, long pendentes, Double mediaHorasResolucao) {
    }

    /** Uma linha da consulta 2 do DQL; mediaDiasResolucao e nula se nenhuma foi resolvida no bairro. */
    public record ResumoBairro(String bairro, long quantidade, long pendentes, double mediaRisco,
                               Double mediaDiasResolucao) {
    }

    public record ResumoTipo(String tipo, long quantidade, long pendentes) {
    }

    public record TotaisPontosAlagados(long total, long ativos) {
    }

    public record TotaisAlertas(long alertasEnviados, long destinatarios, long usuariosAlertados, long lidos) {
    }

    /** Ponto alagado reportado mais de uma vez no mesmo endereco de referencia. */
    public record ReincidenciaPonto(String enderecoReferencia, String bairro, long vezes) {
    }

    /** Combinacao bairro + tipo com mais de uma ocorrencia. */
    public record ReincidenciaBairroTipo(String bairro, String tipo, long quantidade, long pendentes) {
    }

    @FunctionalInterface
    private interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    private final ConnectionFactory connectionFactory;

    public IndicadorDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public TotaisOcorrencias totaisOcorrencias() {
        String sql = "SELECT COUNT(*) AS total, "
                + "NVL(SUM(CASE WHEN st_ocorrencia <> 'RESOLVIDA' THEN 1 ELSE 0 END), 0) AS pendentes, "
                + "AVG(CASE WHEN st_ocorrencia = 'RESOLVIDA' THEN (dt_resolucao - dt_registro) * 24 END) AS media_horas "
                + "FROM T_AB_OCORRENCIA";
        return consultar(sql, rs -> new TotaisOcorrencias(rs.getLong("total"), rs.getLong("pendentes"),
                lerDouble(rs, "media_horas")), "calcular totais de ocorrências").get(0);
    }

    public List<ResumoBairro> porBairro() {
        String sql = "SELECT b.nm_bairro, "
                + "COUNT(*) AS qt_ocorrencias, "
                + "SUM(CASE WHEN o.st_ocorrencia <> 'RESOLVIDA' THEN 1 ELSE 0 END) AS qt_pendentes, "
                + "ROUND(AVG(o.nr_nivel_risco), 1) AS media_risco, "
                + "ROUND(AVG(o.dt_resolucao - o.dt_registro), 1) AS media_dias_resolucao "
                + "FROM T_AB_OCORRENCIA o "
                + "INNER JOIN T_AB_BAIRRO b ON b.id_bairro = o.id_bairro "
                + "GROUP BY b.nm_bairro "
                + "ORDER BY qt_ocorrencias DESC, b.nm_bairro";
        return consultar(sql, rs -> new ResumoBairro(rs.getString("nm_bairro"), rs.getLong("qt_ocorrencias"),
                rs.getLong("qt_pendentes"), rs.getDouble("media_risco"), lerDouble(rs, "media_dias_resolucao")),
                "calcular indicadores por bairro");
    }

    public List<ResumoTipo> porTipo() {
        String sql = "SELECT t.nm_tipo, COUNT(*) AS quantidade, "
                + "SUM(CASE WHEN o.st_ocorrencia <> 'RESOLVIDA' THEN 1 ELSE 0 END) AS pendentes "
                + "FROM T_AB_OCORRENCIA o "
                + "INNER JOIN T_AB_TIPO_OCORRENCIA t ON t.id_tipo_ocorrencia = o.id_tipo_ocorrencia "
                + "GROUP BY t.nm_tipo ORDER BY quantidade DESC, t.nm_tipo";
        return consultar(sql, rs -> new ResumoTipo(rs.getString("nm_tipo"), rs.getLong("quantidade"),
                rs.getLong("pendentes")), "calcular indicadores por tipo");
    }

    public TotaisPontosAlagados totaisPontosAlagados() {
        String sql = "SELECT COUNT(*) AS total, NVL(SUM(CASE WHEN st_ativo = 'S' THEN 1 ELSE 0 END), 0) AS ativos "
                + "FROM T_AB_PONTO_ALAGADO";
        return consultar(sql, rs -> new TotaisPontosAlagados(rs.getLong("total"), rs.getLong("ativos")),
                "calcular totais de pontos alagados").get(0);
    }

    public TotaisAlertas totaisAlertas() {
        String sql = "SELECT (SELECT COUNT(*) FROM T_AB_ALERTA) AS alertas, "
                + "COUNT(*) AS destinatarios, "
                + "COUNT(DISTINCT id_usuario) AS usuarios, "
                + "COUNT(dt_leitura) AS lidos "
                + "FROM T_AB_ALERTA_USUARIO";
        return consultar(sql, rs -> new TotaisAlertas(rs.getLong("alertas"), rs.getLong("destinatarios"),
                rs.getLong("usuarios"), rs.getLong("lidos")), "calcular totais de alertas").get(0);
    }

    public List<ReincidenciaPonto> reincidenciaPontosAlagados() {
        String sql = "SELECT p.ds_endereco_ref, b.nm_bairro, COUNT(*) AS vezes "
                + "FROM T_AB_PONTO_ALAGADO p "
                + "INNER JOIN T_AB_BAIRRO b ON b.id_bairro = p.id_bairro "
                + "GROUP BY p.ds_endereco_ref, b.nm_bairro "
                + "HAVING COUNT(*) > 1 "
                + "ORDER BY vezes DESC, p.ds_endereco_ref";
        return consultar(sql, rs -> new ReincidenciaPonto(rs.getString("ds_endereco_ref"),
                rs.getString("nm_bairro"), rs.getLong("vezes")), "calcular reincidência de pontos alagados");
    }

    public List<ReincidenciaBairroTipo> reincidenciaBairroTipo() {
        String sql = "SELECT b.nm_bairro, t.nm_tipo, COUNT(*) AS quantidade, "
                + "SUM(CASE WHEN o.st_ocorrencia <> 'RESOLVIDA' THEN 1 ELSE 0 END) AS pendentes "
                + "FROM T_AB_OCORRENCIA o "
                + "INNER JOIN T_AB_BAIRRO b ON b.id_bairro = o.id_bairro "
                + "INNER JOIN T_AB_TIPO_OCORRENCIA t ON t.id_tipo_ocorrencia = o.id_tipo_ocorrencia "
                + "GROUP BY b.nm_bairro, t.nm_tipo "
                + "HAVING COUNT(*) > 1 "
                + "ORDER BY quantidade DESC, b.nm_bairro, t.nm_tipo";
        return consultar(sql, rs -> new ReincidenciaBairroTipo(rs.getString("nm_bairro"), rs.getString("nm_tipo"),
                rs.getLong("quantidade"), rs.getLong("pendentes")), "calcular reincidência por bairro e tipo");
    }

    private <T> List<T> consultar(String sql, Mapeador<T> mapeador, String operacao) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<T> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapeador.mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, operacao);
        }
    }

    private static Double lerDouble(ResultSet rs, String coluna) throws SQLException {
        BigDecimal valor = rs.getBigDecimal(coluna);
        return valor == null ? null : valor.doubleValue();
    }
}
