package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.Alerta;
import br.com.fiap.alertabairro.model.NivelAgua;
import br.com.fiap.alertabairro.model.PontoAlagado;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acesso a tabela T_AB_PONTO_ALAGADO. IDs gerados pela sequence SQ_AB_PONTO_ALAGADO.
 *
 * {@link #reportar} grava, numa unica transacao, o ponto alagado, o alerta
 * gerado por ele (T_AB_ALERTA) e os destinatarios (T_AB_ALERTA_USUARIO).
 */
@Repository
public class PontoAlagadoDAO {

    static final String SEQUENCE = "SQ_AB_PONTO_ALAGADO";

    private static final String COLUNAS = "id_ponto_alagado, id_usuario, id_bairro, ds_endereco_ref, nr_latitude, "
            + "nr_longitude, tp_nivel_agua, dt_reporte, st_ativo, dt_encerramento";

    private final ConnectionFactory connectionFactory;
    private final AlertaDAO alertaDAO;
    private final AlertaUsuarioDAO alertaUsuarioDAO;

    public PontoAlagadoDAO(ConnectionFactory connectionFactory, AlertaDAO alertaDAO,
                           AlertaUsuarioDAO alertaUsuarioDAO) {
        this.connectionFactory = connectionFactory;
        this.alertaDAO = alertaDAO;
        this.alertaUsuarioDAO = alertaUsuarioDAO;
    }

    /**
     * Grava ponto + alerta + destinatarios na mesma transacao. Preenche os IDs
     * gerados no ponto e no alerta (o idPontoAlagado do alerta e definido aqui).
     *
     * @return quantidade de destinatarios gravados
     */
    public int reportar(PontoAlagado ponto, Alerta alerta, Collection<Long> idsDestinatarios) {
        return JdbcUtils.emTransacao(connectionFactory, "reportar ponto alagado", conn -> {
            inserir(conn, ponto);
            alerta.setIdPontoAlagado(ponto.getId());
            alertaDAO.inserir(conn, alerta);
            return alertaUsuarioDAO.inserirDestinatarios(conn, alerta.getId(), idsDestinatarios,
                    alerta.getDataEnvio());
        });
    }

    private void inserir(Connection conn, PontoAlagado p) throws SQLException {
        long id = JdbcUtils.proximoId(conn, SEQUENCE);
        String sql = "INSERT INTO T_AB_PONTO_ALAGADO (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, p.getIdUsuario());
            ps.setLong(3, p.getIdBairro());
            ps.setString(4, p.getEnderecoReferencia());
            JdbcUtils.definirCoordenada(ps, 5, p.getLatitude());
            JdbcUtils.definirCoordenada(ps, 6, p.getLongitude());
            ps.setString(7, p.getNivelAgua().name());
            JdbcUtils.definirData(ps, 8, p.getDataReporte());
            ps.setString(9, JdbcUtils.simNao(p.isAtivo()));
            JdbcUtils.definirData(ps, 10, p.getDataEncerramento());
            ps.executeUpdate();
        }
        p.setId(id);
    }

    public Optional<PontoAlagado> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_PONTO_ALAGADO WHERE id_ponto_alagado = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar ponto alagado");
        }
    }

    /** Lista os pontos (somente os ativos, se pedido), do reporte mais recente para o mais antigo. */
    public List<PontoAlagado> listar(boolean somenteAtivos) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_PONTO_ALAGADO"
                + (somenteAtivos ? " WHERE st_ativo = 'S'" : "")
                + " ORDER BY dt_reporte DESC, id_ponto_alagado DESC";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PontoAlagado> pontos = new ArrayList<>();
            while (rs.next()) {
                pontos.add(mapear(rs));
            }
            return pontos;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar pontos alagados");
        }
    }

    /** Pontos pelos IDs (id -> ponto), em uma unica consulta. */
    public Map<Long, PontoAlagado> buscarPorIds(Collection<Long> ids) {
        Map<Long, PontoAlagado> pontos = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return pontos;
        }
        String sql = "SELECT " + COLUNAS + " FROM T_AB_PONTO_ALAGADO WHERE id_ponto_alagado IN ("
                + JdbcUtils.marcadores(ids.size()) + ")";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.definirIds(ps, 1, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PontoAlagado p = mapear(rs);
                    pontos.put(p.getId(), p);
                }
            }
            return pontos;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar pontos alagados");
        }
    }

    /** Encerra o ponto (st_ativo = 'N' + dt_encerramento), somente se ainda estiver ativo. */
    public boolean encerrar(PontoAlagado p) {
        String sql = "UPDATE T_AB_PONTO_ALAGADO SET st_ativo = 'N', dt_encerramento = ? "
                + "WHERE id_ponto_alagado = ? AND st_ativo = 'S'";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.definirData(ps, 1, p.getDataEncerramento());
            ps.setLong(2, p.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "encerrar ponto alagado");
        }
    }

    private PontoAlagado mapear(ResultSet rs) throws SQLException {
        PontoAlagado p = new PontoAlagado();
        p.setId(rs.getLong("id_ponto_alagado"));
        p.setIdUsuario(rs.getLong("id_usuario"));
        p.setIdBairro(rs.getLong("id_bairro"));
        p.setEnderecoReferencia(rs.getString("ds_endereco_ref"));
        p.setLatitude(rs.getDouble("nr_latitude"));
        p.setLongitude(rs.getDouble("nr_longitude"));
        p.setNivelAgua(NivelAgua.valueOf(rs.getString("tp_nivel_agua")));
        p.setDataReporte(JdbcUtils.lerData(rs, "dt_reporte"));
        p.setAtivo(JdbcUtils.lerSimNao(rs, "st_ativo"));
        p.setDataEncerramento(JdbcUtils.lerData(rs, "dt_encerramento"));
        return p;
    }
}
