package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.Alerta;
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
 * Acesso a tabela T_AB_ALERTA. IDs gerados pela sequence SQ_AB_ALERTA.
 * O alerta e criado junto com o ponto alagado, na transacao de
 * {@link PontoAlagadoDAO#reportar}.
 */
@Repository
public class AlertaDAO {

    static final String SEQUENCE = "SQ_AB_ALERTA";

    private static final String COLUNAS = "al.id_alerta, al.id_ponto_alagado, al.ds_mensagem, al.nr_raio_metros, "
            + "al.dt_envio";

    private final ConnectionFactory connectionFactory;

    public AlertaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /** Insere usando a conexao (e a transacao) de quem chama. */
    Alerta inserir(Connection conn, Alerta alerta) throws SQLException {
        long id = JdbcUtils.proximoId(conn, SEQUENCE);
        String sql = "INSERT INTO T_AB_ALERTA (id_alerta, id_ponto_alagado, ds_mensagem, nr_raio_metros, dt_envio) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, alerta.getIdPontoAlagado());
            ps.setString(3, alerta.getMensagem());
            ps.setInt(4, alerta.getRaioMetros());
            JdbcUtils.definirData(ps, 5, alerta.getDataEnvio());
            ps.executeUpdate();
        }
        alerta.setId(id);
        return alerta;
    }

    public Optional<Alerta> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_ALERTA al WHERE al.id_alerta = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar alerta");
        }
    }

    /** Alertas cujo ponto alagado ainda esta ativo, do mais recente para o mais antigo. */
    public List<Alerta> listarAtivos() {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_ALERTA al "
                + "JOIN T_AB_PONTO_ALAGADO p ON p.id_ponto_alagado = al.id_ponto_alagado "
                + "WHERE p.st_ativo = 'S' ORDER BY al.dt_envio DESC, al.id_alerta DESC";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Alerta> alertas = new ArrayList<>();
            while (rs.next()) {
                alertas.add(mapear(rs));
            }
            return alertas;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar alertas ativos");
        }
    }

    /** Alertas pelos IDs (id -> alerta), em uma unica consulta. */
    public Map<Long, Alerta> buscarPorIds(Collection<Long> ids) {
        Map<Long, Alerta> alertas = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return alertas;
        }
        String sql = "SELECT " + COLUNAS + " FROM T_AB_ALERTA al WHERE al.id_alerta IN ("
                + JdbcUtils.marcadores(ids.size()) + ")";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.definirIds(ps, 1, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alerta a = mapear(rs);
                    alertas.put(a.getId(), a);
                }
            }
            return alertas;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar alertas");
        }
    }

    private Alerta mapear(ResultSet rs) throws SQLException {
        return new Alerta(
                rs.getLong("id_alerta"),
                rs.getLong("id_ponto_alagado"),
                rs.getString("ds_mensagem"),
                rs.getInt("nr_raio_metros"),
                JdbcUtils.lerData(rs, "dt_envio"));
    }
}
