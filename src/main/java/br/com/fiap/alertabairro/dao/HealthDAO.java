package br.com.fiap.alertabairro.dao;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Verifica se a conexao com o Oracle esta funcionando (SELECT 1 FROM DUAL).
 */
@Repository
public class HealthDAO {

    private final ConnectionFactory connectionFactory;

    public HealthDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /** Executa uma consulta simples; lanca excecao se o banco nao responder. */
    public void verificarConexao() throws Exception {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM DUAL");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
        }
    }
}
