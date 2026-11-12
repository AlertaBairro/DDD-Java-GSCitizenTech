package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acesso a tabela T_AB_USUARIO. IDs gerados pela sequence SQ_AB_USUARIO.
 * A exclusao e logica (st_ativo = 'N'), porque o usuario e referenciado por
 * ocorrencias, apoios, historico, pontos alagados, alertas e rotas.
 */
@Repository
public class UsuarioDAO {

    static final String SEQUENCE = "SQ_AB_USUARIO";

    private static final String COLUNAS = "id_usuario, id_bairro, nm_usuario, ds_email, ds_senha_hash, "
            + "nr_telefone, tp_perfil, dt_cadastro, st_ativo";

    private final ConnectionFactory connectionFactory;

    public UsuarioDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Usuario inserir(Usuario usuario) {
        String sql = "INSERT INTO T_AB_USUARIO (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection()) {
            long id = JdbcUtils.proximoId(conn, SEQUENCE);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setLong(2, usuario.getIdBairro());
                ps.setString(3, usuario.getNome());
                ps.setString(4, usuario.getEmail());
                ps.setString(5, usuario.getSenhaHash());
                JdbcUtils.definirTexto(ps, 6, usuario.getTelefone());
                ps.setString(7, usuario.getPerfil().name());
                JdbcUtils.definirData(ps, 8, usuario.getDataCadastro());
                ps.setString(9, JdbcUtils.simNao(usuario.isAtivo()));
                ps.executeUpdate();
            }
            usuario.setId(id);
            return usuario;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "cadastrar usuário");
        }
    }

    public Optional<Usuario> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_USUARIO WHERE id_usuario = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar usuário");
        }
    }

    /** Busca pelo e-mail sem diferenciar maiusculas de minusculas. */
    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT " + COLUNAS + " FROM T_AB_USUARIO WHERE LOWER(ds_email) = LOWER(?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar usuário por e-mail");
        }
    }

    /** Nomes dos usuarios informados (id -> nome), em uma unica consulta. */
    public Map<Long, String> buscarNomes(Collection<Long> ids) {
        Map<Long, String> nomes = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return nomes;
        }
        String sql = "SELECT id_usuario, nm_usuario FROM T_AB_USUARIO WHERE id_usuario IN ("
                + JdbcUtils.marcadores(ids.size()) + ")";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.definirIds(ps, 1, ids);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nomes.put(rs.getLong("id_usuario"), rs.getString("nm_usuario"));
                }
            }
            return nomes;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "buscar nomes de usuários");
        }
    }

    /** IDs dos usuarios ativos de um bairro (destinatarios dos alertas). */
    public List<Long> listarIdsAtivosPorBairro(Long idBairro) {
        String sql = "SELECT id_usuario FROM T_AB_USUARIO WHERE id_bairro = ? AND st_ativo = 'S' ORDER BY id_usuario";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idBairro);
            try (ResultSet rs = ps.executeQuery()) {
                List<Long> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
                return ids;
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "listar usuários do bairro");
        }
    }

    /** Atualiza bairro, nome, telefone e hash da senha. */
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE T_AB_USUARIO SET id_bairro = ?, nm_usuario = ?, nr_telefone = ?, ds_senha_hash = ? "
                + "WHERE id_usuario = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuario.getIdBairro());
            ps.setString(2, usuario.getNome());
            JdbcUtils.definirTexto(ps, 3, usuario.getTelefone());
            ps.setString(4, usuario.getSenhaHash());
            ps.setLong(5, usuario.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "atualizar usuário");
        }
    }

    /** Exclusao logica: st_ativo = 'N'. */
    public boolean desativar(Long id) {
        String sql = "UPDATE T_AB_USUARIO SET st_ativo = 'N' WHERE id_usuario = ? AND st_ativo = 'S'";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, "desativar usuário");
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getLong("id_usuario"),
                rs.getLong("id_bairro"),
                rs.getString("nm_usuario"),
                rs.getString("ds_email"),
                rs.getString("ds_senha_hash"),
                rs.getString("nr_telefone"),
                Perfil.valueOf(rs.getString("tp_perfil")),
                JdbcUtils.lerData(rs, "dt_cadastro"),
                JdbcUtils.lerSimNao(rs, "st_ativo"));
    }
}
