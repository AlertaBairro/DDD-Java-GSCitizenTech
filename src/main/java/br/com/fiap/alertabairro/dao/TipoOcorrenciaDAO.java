package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.TipoOcorrencia;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a tabela T_AB_TIPO_OCORRENCIA (somente leitura: os tipos vem do DML do grupo).
 */
@Repository
public class TipoOcorrenciaDAO {

    private static final String COLUNAS = "id_tipo_ocorrencia, nm_tipo, ds_tipo, nr_peso_risco";

    private final ConnectionFactory connectionFactory;

    public TipoOcorrenciaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public List<TipoOcorrencia> listar() {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_TIPO_OCORRENCIA ORDER BY nr_peso_risco DESC, nm_tipo";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<TipoOcorrencia> tipos = new ArrayList<>();
            while (rs.next()) {
                tipos.add(mapear(rs));
            }
            return tipos;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar tipos de ocorrência");
        }
    }

    public Optional<TipoOcorrencia> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_TIPO_OCORRENCIA WHERE id_tipo_ocorrencia = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar tipo de ocorrência");
        }
    }

    private TipoOcorrencia mapear(ResultSet rs) throws SQLException {
        return new TipoOcorrencia(
                rs.getLong("id_tipo_ocorrencia"),
                rs.getString("nm_tipo"),
                rs.getString("ds_tipo"),
                rs.getInt("nr_peso_risco"));
    }
}
