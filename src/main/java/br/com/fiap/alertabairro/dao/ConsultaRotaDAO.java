package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.ConsultaRota;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tabela T_AB_CONSULTA_ROTA. IDs gerados pela sequence SQ_AB_CONSULTA_ROTA.
 */
@Repository
public class ConsultaRotaDAO {

    static final String SEQUENCE = "SQ_AB_CONSULTA_ROTA";

    private static final String COLUNAS = "id_consulta_rota, id_usuario, ds_origem, nr_lat_origem, nr_long_origem, "
            + "ds_destino, nr_lat_destino, nr_long_destino, dt_consulta, st_rota_calculada, nr_pontos_evitados";

    private final ConnectionFactory connectionFactory;

    public ConsultaRotaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public ConsultaRota inserir(ConsultaRota c) {
        String sql = "INSERT INTO T_AB_CONSULTA_ROTA (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection()) {
            long id = JdbcUtils.proximoId(conn, SEQUENCE);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setLong(2, c.getIdUsuario());
                ps.setString(3, c.getOrigem());
                JdbcUtils.definirCoordenada(ps, 4, c.getLatitudeOrigem());
                JdbcUtils.definirCoordenada(ps, 5, c.getLongitudeOrigem());
                ps.setString(6, c.getDestino());
                JdbcUtils.definirCoordenada(ps, 7, c.getLatitudeDestino());
                JdbcUtils.definirCoordenada(ps, 8, c.getLongitudeDestino());
                JdbcUtils.definirData(ps, 9, c.getDataConsulta());
                ps.setString(10, JdbcUtils.simNao(c.isRotaCalculada()));
                ps.setInt(11, c.getPontosEvitados());
                ps.executeUpdate();
            }
            c.setId(id);
            return c;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "registrar consulta de rota");
        }
    }

    /** Historico de consultas do usuario, da mais recente para a mais antiga. */
    public List<ConsultaRota> listarPorUsuario(Long idUsuario) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_CONSULTA_ROTA WHERE id_usuario = ? "
                + "ORDER BY dt_consulta DESC, id_consulta_rota DESC";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                List<ConsultaRota> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar consultas de rota");
        }
    }

    private ConsultaRota mapear(ResultSet rs) throws SQLException {
        ConsultaRota c = new ConsultaRota();
        c.setId(rs.getLong("id_consulta_rota"));
        c.setIdUsuario(rs.getLong("id_usuario"));
        c.setOrigem(rs.getString("ds_origem"));
        c.setLatitudeOrigem(rs.getDouble("nr_lat_origem"));
        c.setLongitudeOrigem(rs.getDouble("nr_long_origem"));
        c.setDestino(rs.getString("ds_destino"));
        c.setLatitudeDestino(rs.getDouble("nr_lat_destino"));
        c.setLongitudeDestino(rs.getDouble("nr_long_destino"));
        c.setDataConsulta(JdbcUtils.lerData(rs, "dt_consulta"));
        c.setRotaCalculada(JdbcUtils.lerSimNao(rs, "st_rota_calculada"));
        c.setPontosEvitados(rs.getInt("nr_pontos_evitados"));
        return c;
    }
}
