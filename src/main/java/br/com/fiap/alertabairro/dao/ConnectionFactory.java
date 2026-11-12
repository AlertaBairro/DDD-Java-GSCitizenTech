package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.PersistenciaException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Fabrica de conexoes JDBC com o Oracle da FIAP.
 *
 * As credenciais NUNCA ficam no codigo: sao lidas das variaveis de ambiente
 * DB_URL, DB_USER e DB_PASSWORD. As conexoes vem de um pool pequeno (HikariCP)
 * criado na primeira utilizacao, o que evita abrir uma conexao nova com o
 * servidor remoto a cada requisicao. Quem chama getConnection() deve fechar a
 * conexao (try-with-resources), devolvendo-a ao pool.
 */
@Component
public class ConnectionFactory {

    public static final String VAR_URL = "DB_URL";
    public static final String VAR_USUARIO = "DB_USER";
    public static final String VAR_SENHA = "DB_PASSWORD";

    private volatile HikariDataSource dataSource;

    public Connection getConnection() {
        try {
            return obterDataSource().getConnection();
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível conectar ao banco de dados Oracle.", e);
        }
    }

    private HikariDataSource obterDataSource() {
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    dataSource = criarDataSource();
                }
            }
        }
        return dataSource;
    }

    private HikariDataSource criarDataSource() {
        String url = lerVariavel(VAR_URL);
        String usuario = lerVariavel(VAR_USUARIO);
        String senha = lerVariavel(VAR_SENHA);

        HikariConfig config = new HikariConfig();
        config.setPoolName("alertabairro-oracle");
        config.setDriverClassName("oracle.jdbc.OracleDriver");
        config.setJdbcUrl(url);
        config.setUsername(usuario);
        config.setPassword(senha);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(15_000);
        config.setIdleTimeout(120_000);
        config.setMaxLifetime(600_000);
        config.setKeepaliveTime(60_000);
        // nao falha a subida da aplicacao se o banco estiver fora do ar
        config.setInitializationFailTimeout(-1);
        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }

    private String lerVariavel(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(nome);
        }
        if (valor == null || valor.isBlank()) {
            throw new PersistenciaException("Variável de ambiente " + nome
                    + " não configurada. Defina DB_URL, DB_USER e DB_PASSWORD.");
        }
        return valor.trim();
    }

    @PreDestroy
    public void fechar() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
