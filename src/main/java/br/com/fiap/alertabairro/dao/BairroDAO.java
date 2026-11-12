package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.PersistenciaException;
import br.com.fiap.alertabairro.model.Bairro;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO da tabela AB_BAIRRO (CRUD com JDBC puro).
 */
@Repository
public class BairroDAO {

    private final ConnectionFactory connectionFactory;

    public BairroDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Bairro inserir(Bairro bairro) {
        String sql = "INSERT INTO AB_BAIRRO (NM_BAIRRO) VALUES (?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_BAIRRO"})) {
            ps.setString(1, bairro.getNome());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    bairro.setId(chaves.getLong(1));
                }
            }
            return bairro;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir bairro.", e);
        }
    }

    public Optional<Bairro> buscarPorId(Long id) {
        String sql = "SELECT ID_BAIRRO, NM_BAIRRO FROM AB_BAIRRO WHERE ID_BAIRRO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar bairro.", e);
        }
    }

    public List<Bairro> listarTodos() {
        String sql = "SELECT ID_BAIRRO, NM_BAIRRO FROM AB_BAIRRO ORDER BY NM_BAIRRO";
        List<Bairro> bairros = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bairros.add(mapear(rs));
            }
            return bairros;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar bairros.", e);
        }
    }

    public boolean atualizar(Bairro bairro) {
        String sql = "UPDATE AB_BAIRRO SET NM_BAIRRO = ? WHERE ID_BAIRRO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bairro.getNome());
            ps.setLong(2, bairro.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar bairro.", e);
        }
    }

    public boolean excluir(Long id) {
        String sql = "DELETE FROM AB_BAIRRO WHERE ID_BAIRRO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao excluir bairro.", e);
        }
    }

    private Bairro mapear(ResultSet rs) throws SQLException {
        return new Bairro(rs.getLong("ID_BAIRRO"), rs.getString("NM_BAIRRO"));
    }
}
