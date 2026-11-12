package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.AlertaUsuario;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a tabela T_AB_ALERTA_USUARIO (chave composta id_alerta + id_usuario).
 * Os destinatarios sao gravados na transacao de {@link PontoAlagadoDAO#reportar}.
 */
@Repository
public class AlertaUsuarioDAO {

    private static final String COLUNAS = "id_alerta, id_usuario, dt_recebimento, dt_leitura";

    private final ConnectionFactory connectionFactory;

    public AlertaUsuarioDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /** Insere um destinatario por usuario (batch), usando a transacao de quem chama. */
    int inserirDestinatarios(Connection conn, Long idAlerta, Collection<Long> idsUsuarios,
                             LocalDateTime dataRecebimento) throws SQLException {
        if (idsUsuarios.isEmpty()) {
            return 0;
        }
        String sql = "INSERT INTO T_AB_ALERTA_USUARIO (" + COLUNAS + ") VALUES (?, ?, ?, NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Long idUsuario : idsUsuarios) {
                ps.setLong(1, idAlerta);
                ps.setLong(2, idUsuario);
                JdbcUtils.definirData(ps, 3, dataRecebimento);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        return idsUsuarios.size();
    }

    /** Alertas recebidos pelo usuario (lidos e nao lidos), do mais recente para o mais antigo. */
    public List<AlertaUsuario> listarPorUsuario(Long idUsuario) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_ALERTA_USUARIO WHERE id_usuario = ? "
                + "ORDER BY dt_recebimento DESC, id_alerta DESC";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                List<AlertaUsuario> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar alertas do usuário");
        }
    }

    public Optional<AlertaUsuario> buscar(Long idAlerta, Long idUsuario) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_ALERTA_USUARIO WHERE id_alerta = ? AND id_usuario = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idAlerta);
            ps.setLong(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar alerta do usuário");
        }
    }

    /** Grava a data de leitura (somente se ainda nao estava lido). */
    public boolean registrarLeitura(AlertaUsuario alertaUsuario) {
        String sql = "UPDATE T_AB_ALERTA_USUARIO SET dt_leitura = ? "
                + "WHERE id_alerta = ? AND id_usuario = ? AND dt_leitura IS NULL";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.definirData(ps, 1, alertaUsuario.getDataLeitura());
            ps.setLong(2, alertaUsuario.getIdAlerta());
            ps.setLong(3, alertaUsuario.getIdUsuario());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "marcar alerta como lido");
        }
    }

    private AlertaUsuario mapear(ResultSet rs) throws SQLException {
        return new AlertaUsuario(
                rs.getLong("id_alerta"),
                rs.getLong("id_usuario"),
                JdbcUtils.lerData(rs, "dt_recebimento"),
                JdbcUtils.lerData(rs, "dt_leitura"));
    }
}
