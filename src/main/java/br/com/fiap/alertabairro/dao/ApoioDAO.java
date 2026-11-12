package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.ConflitoException;
import br.com.fiap.alertabairro.exception.PersistenciaException;
import br.com.fiap.alertabairro.model.Apoio;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * DAO da tabela AB_APOIO (CRUD com JDBC puro).
 */
@Repository
public class ApoioDAO {

    private static final String COLUNAS = "ID_APOIO, ID_USUARIO, ID_OCORRENCIA, DT_APOIO";

    private final ConnectionFactory connectionFactory;

    public ApoioDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Apoio inserir(Apoio apoio) {
        String sql = "INSERT INTO AB_APOIO (ID_USUARIO, ID_OCORRENCIA, DT_APOIO) VALUES (?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_APOIO"})) {
            ps.setLong(1, apoio.getUsuarioId());
            ps.setLong(2, apoio.getOcorrenciaId());
            JdbcUtils.definirData(ps, 3, apoio.getDataApoio());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    apoio.setId(chaves.getLong(1));
                }
            }
            return apoio;
        } catch (SQLIntegrityConstraintViolationException e) {
            // ORA-00001: violacao da UNIQUE (usuario, ocorrencia) em requisicoes simultaneas
            if (e.getErrorCode() == 1) {
                throw new ConflitoException("Você já apoiou esta ocorrência.");
            }
            throw new PersistenciaException("Erro ao registrar apoio.", e);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao registrar apoio.", e);
        }
    }

    public Optional<Apoio> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM AB_APOIO WHERE ID_APOIO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar apoio.", e);
        }
    }

    public boolean existe(Long usuarioId, Long ocorrenciaId) {
        String sql = "SELECT COUNT(*) FROM AB_APOIO WHERE ID_USUARIO = ? AND ID_OCORRENCIA = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ps.setLong(2, ocorrenciaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao verificar apoio.", e);
        }
    }

    public List<Apoio> listarTodos() {
        return consultar("SELECT " + COLUNAS + " FROM AB_APOIO ORDER BY DT_APOIO DESC", null);
    }

    public List<Apoio> listarPorUsuario(Long usuarioId) {
        return consultar("SELECT " + COLUNAS + " FROM AB_APOIO WHERE ID_USUARIO = ? ORDER BY DT_APOIO DESC",
                usuarioId);
    }

    public List<Apoio> listarPorOcorrencia(Long ocorrenciaId) {
        return consultar("SELECT " + COLUNAS + " FROM AB_APOIO WHERE ID_OCORRENCIA = ? ORDER BY DT_APOIO DESC",
                ocorrenciaId);
    }

    /** Ids das ocorrencias que o usuario ja apoiou (usado no campo apoiadaPorMim). */
    public Set<Long> listarIdsOcorrenciasApoiadas(Long usuarioId) {
        String sql = "SELECT ID_OCORRENCIA FROM AB_APOIO WHERE ID_USUARIO = ?";
        Set<Long> ids = new HashSet<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
            }
            return ids;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar apoios do usuário.", e);
        }
    }

    public boolean atualizar(Apoio apoio) {
        String sql = "UPDATE AB_APOIO SET DT_APOIO = ? WHERE ID_APOIO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.definirData(ps, 1, apoio.getDataApoio());
            ps.setLong(2, apoio.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar apoio.", e);
        }
    }

    public boolean excluir(Long id) {
        return executarExclusao("DELETE FROM AB_APOIO WHERE ID_APOIO = ?", id) > 0;
    }

    public boolean excluir(Long usuarioId, Long ocorrenciaId) {
        String sql = "DELETE FROM AB_APOIO WHERE ID_USUARIO = ? AND ID_OCORRENCIA = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ps.setLong(2, ocorrenciaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao retirar apoio.", e);
        }
    }

    public int excluirPorOcorrencia(Long ocorrenciaId) {
        return executarExclusao("DELETE FROM AB_APOIO WHERE ID_OCORRENCIA = ?", ocorrenciaId);
    }

    public int excluirPorUsuario(Long usuarioId) {
        return executarExclusao("DELETE FROM AB_APOIO WHERE ID_USUARIO = ?", usuarioId);
    }

    private int executarExclusao(String sql, Long id) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao excluir apoios.", e);
        }
    }

    private List<Apoio> consultar(String sql, Long parametro) {
        List<Apoio> apoios = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setLong(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    apoios.add(mapear(rs));
                }
            }
            return apoios;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar apoios.", e);
        }
    }

    private Apoio mapear(ResultSet rs) throws SQLException {
        return new Apoio(
                rs.getLong("ID_APOIO"),
                rs.getLong("ID_USUARIO"),
                rs.getLong("ID_OCORRENCIA"),
                JdbcUtils.lerData(rs, "DT_APOIO"));
    }
}
