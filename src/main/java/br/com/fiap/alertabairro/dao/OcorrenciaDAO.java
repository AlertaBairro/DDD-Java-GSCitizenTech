package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.PersistenciaException;
import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import br.com.fiap.alertabairro.model.TipoProblema;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO da tabela AB_OCORRENCIA (CRUD com JDBC puro).
 * As consultas fazem JOIN com bairro, tipo de problema e usuario para
 * devolver a ocorrencia completa.
 */
@Repository
public class OcorrenciaDAO {

    private static final String SELECT_COMPLETO = """
            SELECT O.ID_OCORRENCIA, O.DS_OBSERVACOES, O.DT_CRIACAO, O.QT_APOIOS, O.ST_OCORRENCIA,
                   O.DT_RESOLUCAO, O.NR_LATITUDE, O.NR_LONGITUDE,
                   B.ID_BAIRRO, B.NM_BAIRRO,
                   T.CD_TIPO, T.NM_TIPO, T.NR_PESO, T.FL_ALAGAMENTO,
                   U.ID_USUARIO, U.NM_USUARIO
              FROM AB_OCORRENCIA O
              JOIN AB_BAIRRO B ON B.ID_BAIRRO = O.ID_BAIRRO
              JOIN AB_TIPO_PROBLEMA T ON T.CD_TIPO = O.CD_TIPO
              JOIN AB_USUARIO U ON U.ID_USUARIO = O.ID_USUARIO
            """;

    private static final String ORDEM_MAIS_RECENTES = " ORDER BY O.DT_CRIACAO DESC, O.ID_OCORRENCIA DESC";

    private final ConnectionFactory connectionFactory;

    public OcorrenciaDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Ocorrencia inserir(Ocorrencia ocorrencia) {
        String sql = """
                INSERT INTO AB_OCORRENCIA (ID_BAIRRO, CD_TIPO, ID_USUARIO, DS_OBSERVACOES, DT_CRIACAO,
                                           QT_APOIOS, ST_OCORRENCIA, DT_RESOLUCAO, NR_LATITUDE, NR_LONGITUDE)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_OCORRENCIA"})) {
            ps.setLong(1, ocorrencia.getBairro().getId());
            ps.setString(2, ocorrencia.getTipo().getCodigo());
            ps.setLong(3, ocorrencia.getUsuario().getId());
            JdbcUtils.definirTexto(ps, 4, ocorrencia.getObservacoes());
            JdbcUtils.definirData(ps, 5, ocorrencia.getDataCriacao());
            ps.setInt(6, ocorrencia.getQuantidadeApoios());
            ps.setString(7, ocorrencia.getStatus().name());
            JdbcUtils.definirData(ps, 8, ocorrencia.getDataResolucao());
            JdbcUtils.definirDouble(ps, 9, ocorrencia.getLatitude());
            JdbcUtils.definirDouble(ps, 10, ocorrencia.getLongitude());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    ocorrencia.setId(chaves.getLong(1));
                }
            }
            return ocorrencia;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir ocorrência.", e);
        }
    }

    public Optional<Ocorrencia> buscarPorId(Long id) {
        String sql = SELECT_COMPLETO + " WHERE O.ID_OCORRENCIA = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar ocorrência.", e);
        }
    }

    public List<Ocorrencia> listarTodas() {
        return listar(null, null, null);
    }

    /**
     * Lista as ocorrencias das mais recentes para as mais antigas,
     * aplicando os filtros que forem informados (os nulos sao ignorados).
     */
    public List<Ocorrencia> listar(Long bairroId, String codigoTipo, StatusOcorrencia status) {
        StringBuilder sql = new StringBuilder(SELECT_COMPLETO).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (bairroId != null) {
            sql.append(" AND O.ID_BAIRRO = ?");
            parametros.add(bairroId);
        }
        if (codigoTipo != null) {
            sql.append(" AND O.CD_TIPO = ?");
            parametros.add(codigoTipo);
        }
        if (status != null) {
            sql.append(" AND O.ST_OCORRENCIA = ?");
            parametros.add(status.name());
        }
        sql.append(ORDEM_MAIS_RECENTES);
        return consultar(sql.toString(), parametros);
    }

    /** Ocorrencias de alagamento que ainda nao foram resolvidas (mais recentes primeiro). */
    public List<Ocorrencia> listarAlagamentosAtivos() {
        String sql = SELECT_COMPLETO
                + " WHERE T.FL_ALAGAMENTO = 'S' AND O.ST_OCORRENCIA <> 'RESOLVIDA'"
                + ORDEM_MAIS_RECENTES;
        return consultar(sql, List.of());
    }

    public List<Ocorrencia> listarPorUsuario(Long usuarioId) {
        String sql = SELECT_COMPLETO + " WHERE O.ID_USUARIO = ?" + ORDEM_MAIS_RECENTES;
        return consultar(sql, List.of(usuarioId));
    }

    /** Atualiza os dados editaveis pelo morador: bairro, tipo, observacoes e localizacao. */
    public boolean atualizar(Ocorrencia ocorrencia) {
        String sql = """
                UPDATE AB_OCORRENCIA
                   SET ID_BAIRRO = ?, CD_TIPO = ?, DS_OBSERVACOES = ?, NR_LATITUDE = ?, NR_LONGITUDE = ?
                 WHERE ID_OCORRENCIA = ?
                """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ocorrencia.getBairro().getId());
            ps.setString(2, ocorrencia.getTipo().getCodigo());
            JdbcUtils.definirTexto(ps, 3, ocorrencia.getObservacoes());
            JdbcUtils.definirDouble(ps, 4, ocorrencia.getLatitude());
            JdbcUtils.definirDouble(ps, 5, ocorrencia.getLongitude());
            ps.setLong(6, ocorrencia.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar ocorrência.", e);
        }
    }

    public boolean atualizarStatus(Long id, StatusOcorrencia status, OffsetDateTime dataResolucao) {
        String sql = "UPDATE AB_OCORRENCIA SET ST_OCORRENCIA = ?, DT_RESOLUCAO = ? WHERE ID_OCORRENCIA = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            JdbcUtils.definirData(ps, 2, dataResolucao);
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar status da ocorrência.", e);
        }
    }

    /**
     * Soma (delta positivo) ou subtrai (delta negativo) apoios do contador,
     * de forma atomica no banco e sem deixar o contador negativo.
     */
    public boolean alterarQuantidadeApoios(Long id, int delta) {
        String sql = "UPDATE AB_OCORRENCIA SET QT_APOIOS = GREATEST(QT_APOIOS + ?, 0) WHERE ID_OCORRENCIA = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar apoios da ocorrência.", e);
        }
    }

    public boolean excluir(Long id) {
        String sql = "DELETE FROM AB_OCORRENCIA WHERE ID_OCORRENCIA = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao excluir ocorrência.", e);
        }
    }

    public int excluirPorUsuario(Long usuarioId) {
        String sql = "DELETE FROM AB_OCORRENCIA WHERE ID_USUARIO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao excluir ocorrências do usuário.", e);
        }
    }

    private List<Ocorrencia> consultar(String sql, List<Object> parametros) {
        List<Ocorrencia> ocorrencias = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ocorrencias.add(mapear(rs));
                }
            }
            return ocorrencias;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar ocorrências.", e);
        }
    }

    private Ocorrencia mapear(ResultSet rs) throws SQLException {
        Bairro bairro = new Bairro(rs.getLong("ID_BAIRRO"), rs.getString("NM_BAIRRO"));
        TipoProblema tipo = new TipoProblema(
                rs.getString("CD_TIPO"),
                rs.getString("NM_TIPO"),
                rs.getInt("NR_PESO"),
                "S".equals(rs.getString("FL_ALAGAMENTO")));
        Usuario autor = new Usuario();
        autor.setId(rs.getLong("ID_USUARIO"));
        autor.setNome(rs.getString("NM_USUARIO"));

        return new Ocorrencia(
                rs.getLong("ID_OCORRENCIA"),
                bairro,
                tipo,
                autor,
                rs.getString("DS_OBSERVACOES"),
                JdbcUtils.lerData(rs, "DT_CRIACAO"),
                rs.getInt("QT_APOIOS"),
                StatusOcorrencia.valueOf(rs.getString("ST_OCORRENCIA")),
                JdbcUtils.lerData(rs, "DT_RESOLUCAO"),
                JdbcUtils.lerDouble(rs, "NR_LATITUDE"),
                JdbcUtils.lerDouble(rs, "NR_LONGITUDE"));
    }
}
