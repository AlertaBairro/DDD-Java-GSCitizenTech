package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.HistoricoStatus;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tabela T_AB_HISTORICO_STATUS. IDs gerados pela sequence
 * SQ_AB_HISTORICO_STATUS. A gravacao junto com a mudanca de status da
 * ocorrencia (mesma transacao) e feita por {@link OcorrenciaDAO#alterarStatus}.
 */
@Repository
public class HistoricoStatusDAO {

    static final String SEQUENCE = "SQ_AB_HISTORICO_STATUS";

    private static final String COLUNAS = "id_historico, id_ocorrencia, id_usuario, st_anterior, st_novo, "
            + "dt_alteracao, ds_observacao";

    private final ConnectionFactory connectionFactory;

    public HistoricoStatusDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public HistoricoStatus inserir(HistoricoStatus historico) {
        try (Connection conn = connectionFactory.getConnection()) {
            return inserir(conn, historico);
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "registrar histórico de status");
        }
    }

    /** Insere usando a conexao (e a transacao) de quem chama. */
    HistoricoStatus inserir(Connection conn, HistoricoStatus historico) throws SQLException {
        long id = JdbcUtils.proximoId(conn, SEQUENCE);
        String sql = "INSERT INTO T_AB_HISTORICO_STATUS (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, historico.getIdOcorrencia());
            ps.setLong(3, historico.getIdUsuario());
            ps.setString(4, historico.getStatusAnterior().name());
            ps.setString(5, historico.getStatusNovo().name());
            JdbcUtils.definirData(ps, 6, historico.getDataAlteracao());
            JdbcUtils.definirTexto(ps, 7, historico.getObservacao());
            ps.executeUpdate();
        }
        historico.setId(id);
        return historico;
    }

    /** Historico de uma ocorrencia, do mais antigo para o mais recente. */
    public List<HistoricoStatus> listarPorOcorrencia(Long idOcorrencia) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_HISTORICO_STATUS WHERE id_ocorrencia = ? "
                + "ORDER BY dt_alteracao, id_historico";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                List<HistoricoStatus> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new HistoricoStatus(
                            rs.getLong("id_historico"),
                            rs.getLong("id_ocorrencia"),
                            rs.getLong("id_usuario"),
                            StatusOcorrencia.valueOf(rs.getString("st_anterior")),
                            StatusOcorrencia.valueOf(rs.getString("st_novo")),
                            JdbcUtils.lerData(rs, "dt_alteracao"),
                            rs.getString("ds_observacao")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar histórico de status");
        }
    }

    /** Remove o historico de uma ocorrencia (usado na exclusao, dentro da transacao). */
    int excluirPorOcorrencia(Connection conn, Long idOcorrencia) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM T_AB_HISTORICO_STATUS WHERE id_ocorrencia = ?")) {
            ps.setLong(1, idOcorrencia);
            return ps.executeUpdate();
        }
    }
}
