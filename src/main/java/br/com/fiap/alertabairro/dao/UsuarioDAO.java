package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.PersistenciaException;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO da tabela AB_USUARIO (CRUD com JDBC puro).
 */
@Repository
public class UsuarioDAO {

    private static final String COLUNAS =
            "ID_USUARIO, NM_USUARIO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL, DT_CADASTRO";

    private final ConnectionFactory connectionFactory;

    public UsuarioDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Usuario inserir(Usuario usuario) {
        String sql = "INSERT INTO AB_USUARIO (NM_USUARIO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_USUARIO"})) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenhaHash());
            ps.setString(4, usuario.getPerfil().name());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    usuario.setId(chaves.getLong(1));
                }
            }
            return usuario;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir usuário.", e);
        }
    }

    public Optional<Usuario> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM AB_USUARIO WHERE ID_USUARIO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar usuário.", e);
        }
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT " + COLUNAS + " FROM AB_USUARIO WHERE LOWER(DS_EMAIL) = LOWER(?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar usuário por e-mail.", e);
        }
    }

    public List<Usuario> listarTodos() {
        String sql = "SELECT " + COLUNAS + " FROM AB_USUARIO ORDER BY NM_USUARIO";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapear(rs));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar usuários.", e);
        }
    }

    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE AB_USUARIO SET NM_USUARIO = ?, DS_EMAIL = ?, DS_SENHA_HASH = ?, TP_PERFIL = ? "
                + "WHERE ID_USUARIO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenhaHash());
            ps.setString(4, usuario.getPerfil().name());
            ps.setLong(5, usuario.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar usuário.", e);
        }
    }

    public boolean excluir(Long id) {
        String sql = "DELETE FROM AB_USUARIO WHERE ID_USUARIO = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao excluir usuário.", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(
                rs.getLong("ID_USUARIO"),
                rs.getString("NM_USUARIO"),
                rs.getString("DS_EMAIL"),
                rs.getString("DS_SENHA_HASH"),
                Perfil.valueOf(rs.getString("TP_PERFIL")));
        usuario.setDataCadastro(JdbcUtils.lerData(rs, "DT_CADASTRO"));
        return usuario;
    }
}
