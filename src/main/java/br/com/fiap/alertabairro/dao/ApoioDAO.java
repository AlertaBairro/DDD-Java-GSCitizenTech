package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.Apoio;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Acesso a tabela T_AB_APOIO (chave composta id_ocorrencia + id_usuario).
 * Apoio duplicado viola PK_AB_APOIO e vira HTTP 409 pelo TradutorErroOracle.
 */
@Repository
public class ApoioDAO {

    private final ConnectionFactory connectionFactory;

    public ApoioDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Apoio inserir(Apoio apoio) {
        String sql = "INSERT INTO T_AB_APOIO (id_ocorrencia, id_usuario, dt_apoio) VALUES (?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, apoio.getIdOcorrencia());
            ps.setLong(2, apoio.getIdUsuario());
            JdbcUtils.definirData(ps, 3, apoio.getDataApoio());
            ps.executeUpdate();
            return apoio;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "registrar apoio");
        }
    }

    public boolean existe(Long idOcorrencia, Long idUsuario) {
        String sql = "SELECT 1 FROM T_AB_APOIO WHERE id_ocorrencia = ? AND id_usuario = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idOcorrencia);
            ps.setLong(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "verificar apoio");
        }
    }

    public List<Apoio> listarPorOcorrencia(Long idOcorrencia) {
        String sql = "SELECT id_ocorrencia, id_usuario, dt_apoio FROM T_AB_APOIO WHERE id_ocorrencia = ? "
                + "ORDER BY dt_apoio";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                List<Apoio> apoios = new ArrayList<>();
                while (rs.next()) {
                    apoios.add(new Apoio(rs.getLong("id_ocorrencia"), rs.getLong("id_usuario"),
                            JdbcUtils.lerData(rs, "dt_apoio")));
                }
                return apoios;
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar apoios");
        }
    }

    /** IDs das ocorrencias que o usuario apoiou (para o campo apoiadaPorMim). */
    public Set<Long> listarIdsOcorrenciasApoiadas(Long idUsuario) {
        String sql = "SELECT id_ocorrencia FROM T_AB_APOIO WHERE id_usuario = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                Set<Long> ids = new HashSet<>();
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
                return ids;
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar apoios do usuário");
        }
    }

    public boolean excluir(Long idOcorrencia, Long idUsuario) {
        String sql = "DELETE FROM T_AB_APOIO WHERE id_ocorrencia = ? AND id_usuario = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idOcorrencia);
            ps.setLong(2, idUsuario);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "retirar apoio");
        }
    }

    /** Remove os apoios de uma ocorrencia (usado na exclusao, dentro da transacao). */
    int excluirPorOcorrencia(Connection conn, Long idOcorrencia) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM T_AB_APOIO WHERE id_ocorrencia = ?")) {
            ps.setLong(1, idOcorrencia);
            return ps.executeUpdate();
        }
    }
}
