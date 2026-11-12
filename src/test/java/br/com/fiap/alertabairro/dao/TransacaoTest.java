package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.ConflitoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransacaoTest {

    private ConnectionFactory factory;
    private Connection conn;

    @BeforeEach
    void preparar() throws SQLException {
        factory = mock(ConnectionFactory.class);
        conn = mock(Connection.class);
        when(factory.getConnection()).thenReturn(conn);
        when(conn.getAutoCommit()).thenReturn(true);
    }

    @Test
    @DisplayName("Sucesso: desliga o auto-commit, faz commit e restaura")
    void commitNoSucesso() throws SQLException {
        String resultado = JdbcUtils.emTransacao(factory, "testar", c -> "ok");

        assertThat(resultado).isEqualTo("ok");
        verify(conn).setAutoCommit(false);
        verify(conn).commit();
        verify(conn, never()).rollback();
        verify(conn).setAutoCommit(true);
        verify(conn).close();
    }

    @Test
    @DisplayName("Erro do Oracle: faz rollback e traduz a exceção (ORA-00001 -> 409)")
    void rollbackNoErro() throws SQLException {
        assertThatThrownBy(() -> JdbcUtils.emTransacao(factory, "testar", c -> {
            throw new SQLException("ORA-00001: unique constraint (PK_AB_APOIO) violated", "23000", 1);
        })).isInstanceOf(ConflitoException.class);

        verify(conn).rollback();
        verify(conn, never()).commit();
        verify(conn).setAutoCommit(true);
    }

    @Test
    @DisplayName("Marcadores da cláusula IN")
    void marcadores() {
        assertThat(JdbcUtils.marcadores(3)).isEqualTo("?, ?, ?");
    }
}
