package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.HistoricoStatus;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a tabela T_AB_OCORRENCIA. IDs gerados pela sequence SQ_AB_OCORRENCIA.
 * A quantidade de apoios e contada em T_AB_APOIO em cada consulta.
 *
 * Operacoes em mais de uma tabela rodam numa unica transacao:
 * <ul>
 *   <li>{@link #alterarStatus}: UPDATE da ocorrencia + INSERT no historico;</li>
 *   <li>{@link #excluir}: DELETE dos apoios, do historico e da ocorrencia
 *       (as FKs do modelo nao tem ON DELETE CASCADE).</li>
 * </ul>
 */
@Repository
public class OcorrenciaDAO {

    static final String SEQUENCE = "SQ_AB_OCORRENCIA";

    private static final String COLUNAS = "o.id_ocorrencia, o.id_usuario, o.id_tipo_ocorrencia, o.id_bairro, "
            + "o.ds_ocorrencia, o.ds_endereco_ref, o.nr_latitude, o.nr_longitude, o.ds_url_foto, o.dt_registro, "
            + "o.nr_nivel_risco, o.st_ocorrencia, o.dt_resolucao";

    private static final String SELECT = "SELECT " + COLUNAS
            + ", (SELECT COUNT(*) FROM T_AB_APOIO a WHERE a.id_ocorrencia = o.id_ocorrencia) AS qt_apoios "
            + "FROM T_AB_OCORRENCIA o";

    private final ConnectionFactory connectionFactory;
    private final HistoricoStatusDAO historicoStatusDAO;
    private final ApoioDAO apoioDAO;

    public OcorrenciaDAO(ConnectionFactory connectionFactory, HistoricoStatusDAO historicoStatusDAO,
                         ApoioDAO apoioDAO) {
        this.connectionFactory = connectionFactory;
        this.historicoStatusDAO = historicoStatusDAO;
        this.apoioDAO = apoioDAO;
    }

    public Ocorrencia inserir(Ocorrencia o) {
        String sql = "INSERT INTO T_AB_OCORRENCIA (id_ocorrencia, id_usuario, id_tipo_ocorrencia, id_bairro, "
                + "ds_ocorrencia, ds_endereco_ref, nr_latitude, nr_longitude, ds_url_foto, dt_registro, "
                + "nr_nivel_risco, st_ocorrencia, dt_resolucao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection()) {
            long id = JdbcUtils.proximoId(conn, SEQUENCE);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setLong(2, o.getIdUsuario());
                ps.setLong(3, o.getIdTipoOcorrencia());
                ps.setLong(4, o.getIdBairro());
                JdbcUtils.definirTexto(ps, 5, o.getDescricao());
                ps.setString(6, o.getEnderecoReferencia());
                JdbcUtils.definirCoordenada(ps, 7, o.getLatitude());
                JdbcUtils.definirCoordenada(ps, 8, o.getLongitude());
                JdbcUtils.definirTexto(ps, 9, o.getUrlFoto());
                JdbcUtils.definirData(ps, 10, o.getDataRegistro());
                ps.setInt(11, o.getNivelRisco());
                ps.setString(12, o.getStatus().name());
                JdbcUtils.definirData(ps, 13, o.getDataResolucao());
                ps.executeUpdate();
            }
            o.setId(id);
            return o;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "registrar ocorrência");
        }
    }

    public Optional<Ocorrencia> buscarPorId(Long id) {
        String sql = SELECT + " WHERE o.id_ocorrencia = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar ocorrência");
        }
    }

    /**
     * Lista com filtros opcionais (null = sem filtro), da mais recente para a mais antiga.
     */
    public List<Ocorrencia> listar(Long idBairro, Long idTipo, StatusOcorrencia status) {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (idBairro != null) {
            sql.append(" AND o.id_bairro = ?");
            parametros.add(idBairro);
        }
        if (idTipo != null) {
            sql.append(" AND o.id_tipo_ocorrencia = ?");
            parametros.add(idTipo);
        }
        if (status != null) {
            sql.append(" AND o.st_ocorrencia = ?");
            parametros.add(status.name());
        }
        sql.append(" ORDER BY o.dt_registro DESC, o.id_ocorrencia DESC");

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Ocorrencia> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar ocorrências");
        }
    }

    public List<Ocorrencia> listarTodas() {
        return listar(null, null, null);
    }

    /** Atualiza os dados editaveis pelo autor (tipo, bairro, textos, coordenadas e risco). */
    public boolean atualizar(Ocorrencia o) {
        String sql = "UPDATE T_AB_OCORRENCIA SET id_tipo_ocorrencia = ?, id_bairro = ?, ds_ocorrencia = ?, "
                + "ds_endereco_ref = ?, nr_latitude = ?, nr_longitude = ?, ds_url_foto = ?, nr_nivel_risco = ? "
                + "WHERE id_ocorrencia = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, o.getIdTipoOcorrencia());
            ps.setLong(2, o.getIdBairro());
            JdbcUtils.definirTexto(ps, 3, o.getDescricao());
            ps.setString(4, o.getEnderecoReferencia());
            JdbcUtils.definirCoordenada(ps, 5, o.getLatitude());
            JdbcUtils.definirCoordenada(ps, 6, o.getLongitude());
            JdbcUtils.definirTexto(ps, 7, o.getUrlFoto());
            ps.setInt(8, o.getNivelRisco());
            ps.setLong(9, o.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "atualizar ocorrência");
        }
    }

    /**
     * Grava o novo status (e a data de resolucao) e a linha de historico na
     * mesma transacao. O UPDATE so acontece se o status no banco ainda for o
     * anterior, evitando sobrescrever uma alteracao concorrente.
     *
     * @return false se a ocorrencia nao existe mais ou o status mudou no meio tempo
     */
    public boolean alterarStatus(Ocorrencia o, HistoricoStatus historico) {
        String sql = "UPDATE T_AB_OCORRENCIA SET st_ocorrencia = ?, dt_resolucao = ? "
                + "WHERE id_ocorrencia = ? AND st_ocorrencia = ?";
        return JdbcUtils.emTransacao(connectionFactory, "alterar o status da ocorrência", conn -> {
            int linhas;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, o.getStatus().name());
                JdbcUtils.definirData(ps, 2, o.getDataResolucao());
                ps.setLong(3, o.getId());
                ps.setString(4, historico.getStatusAnterior().name());
                linhas = ps.executeUpdate();
            }
            if (linhas != 1) {
                conn.rollback();
                return false;
            }
            historicoStatusDAO.inserir(conn, historico);
            return true;
        });
    }

    /** Exclui apoios, historico e a ocorrencia na mesma transacao. */
    public boolean excluir(Long id) {
        return JdbcUtils.emTransacao(connectionFactory, "excluir ocorrência", conn -> {
            apoioDAO.excluirPorOcorrencia(conn, id);
            historicoStatusDAO.excluirPorOcorrencia(conn, id);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM T_AB_OCORRENCIA WHERE id_ocorrencia = ?")) {
                ps.setLong(1, id);
                return ps.executeUpdate() == 1;
            }
        });
    }

    private Ocorrencia mapear(ResultSet rs) throws SQLException {
        Ocorrencia o = new Ocorrencia();
        o.setId(rs.getLong("id_ocorrencia"));
        o.setIdUsuario(rs.getLong("id_usuario"));
        o.setIdTipoOcorrencia(rs.getLong("id_tipo_ocorrencia"));
        o.setIdBairro(rs.getLong("id_bairro"));
        o.setDescricao(rs.getString("ds_ocorrencia"));
        o.setEnderecoReferencia(rs.getString("ds_endereco_ref"));
        o.setLatitude(rs.getDouble("nr_latitude"));
        o.setLongitude(rs.getDouble("nr_longitude"));
        o.setUrlFoto(rs.getString("ds_url_foto"));
        o.setDataRegistro(JdbcUtils.lerData(rs, "dt_registro"));
        o.setNivelRisco(rs.getInt("nr_nivel_risco"));
        o.setStatus(StatusOcorrencia.valueOf(rs.getString("st_ocorrencia")));
        o.setDataResolucao(JdbcUtils.lerData(rs, "dt_resolucao"));
        o.setQuantidadeApoios(rs.getInt("qt_apoios"));
        return o;
    }
}
