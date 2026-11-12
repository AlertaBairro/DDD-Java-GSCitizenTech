package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.Zona;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a tabela T_AB_BAIRRO (somente leitura: os bairros vem do DML do grupo).
 */
@Repository
public class BairroDAO {

    private static final String COLUNAS = "id_bairro, nm_bairro, nm_zona, nm_subprefeitura";

    private final ConnectionFactory connectionFactory;

    public BairroDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public List<Bairro> listar() {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_BAIRRO ORDER BY nm_bairro";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Bairro> bairros = new ArrayList<>();
            while (rs.next()) {
                bairros.add(mapear(rs));
            }
            return bairros;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar bairros");
        }
    }

    public Optional<Bairro> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_BAIRRO WHERE id_bairro = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar bairro");
        }
    }

    private Bairro mapear(ResultSet rs) throws SQLException {
        return new Bairro(
                rs.getLong("id_bairro"),
                rs.getString("nm_bairro"),
                Zona.valueOf(rs.getString("nm_zona")),
                rs.getString("nm_subprefeitura"));
    }
}
