package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.PersistenciaException;
import br.com.fiap.alertabairro.model.TipoProblema;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO da tabela AB_TIPO_PROBLEMA (CRUD com JDBC puro).
 */
@Repository
public class TipoProblemaDAO {

    private static final String COLUNAS = "CD_TIPO, NM_TIPO, NR_PESO, FL_ALAGAMENTO";

    private final ConnectionFactory connectionFactory;

    public TipoProblemaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public TipoProblema inserir(TipoProblema tipo) {
        String sql = "INSERT INTO AB_TIPO_PROBLEMA (" + COLUNAS + ") VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo.getCodigo());
            ps.setString(2, tipo.getNome());
            ps.setInt(3, tipo.getPeso());
            ps.setString(4, tipo.isAlagamento() ? "S" : "N");
            ps.executeUpdate();
            return tipo;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir tipo de problema.", e);
        }
    }

    public Optional<TipoProblema> buscarPorCodigo(String codigo) {
        String sql = "SELECT " + COLUNAS + " FROM AB_TIPO_PROBLEMA WHERE CD_TIPO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar tipo de problema.", e);
        }
    }

    public List<TipoProblema> listarTodos() {
        String sql = "SELECT " + COLUNAS + " FROM AB_TIPO_PROBLEMA ORDER BY NR_PESO DESC, NM_TIPO";
        List<TipoProblema> tipos = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tipos.add(mapear(rs));
            }
            return tipos;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar tipos de problema.", e);
        }
    }

    public boolean atualizar(TipoProblema tipo) {
        String sql = "UPDATE AB_TIPO_PROBLEMA SET NM_TIPO = ?, NR_PESO = ?, FL_ALAGAMENTO = ? WHERE CD_TIPO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo.getNome());
            ps.setInt(2, tipo.getPeso());
            ps.setString(3, tipo.isAlagamento() ? "S" : "N");
            ps.setString(4, tipo.getCodigo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar tipo de problema.", e);
        }
    }

    public boolean excluir(String codigo) {
        String sql = "DELETE FROM AB_TIPO_PROBLEMA WHERE CD_TIPO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao excluir tipo de problema.", e);
        }
    }

    private TipoProblema mapear(ResultSet rs) throws SQLException {
        return new TipoProblema(
                rs.getString("CD_TIPO"),
                rs.getString("NM_TIPO"),
                rs.getInt("NR_PESO"),
                "S".equals(rs.getString("FL_ALAGAMENTO")));
    }
}
