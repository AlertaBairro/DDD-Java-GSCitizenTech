package br.com.fiap.alertabairro.dao;

import br.com.fiap.alertabairro.exception.TradutorErroOracle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * Funcoes auxiliares de JDBC compartilhadas pelos DAOs: conversao de DATE,
 * de CHAR(1) 'S'/'N', de NUMBER(9,6), geracao de IDs pelas sequences e
 * execucao de varias instrucoes na mesma transacao.
 */
final class JdbcUtils {

    static final String SIM = "S";
    static final String NAO = "N";

    private JdbcUtils() {
    }

    // ------------------------------------------------------------------
    // Conversoes
    // ------------------------------------------------------------------

    static LocalDateTime lerData(ResultSet rs, String coluna) throws SQLException {
        Timestamp ts = rs.getTimestamp(coluna);
        return ts == null ? null : ts.toLocalDateTime();
    }

    static void definirData(PreparedStatement ps, int indice, LocalDateTime data) throws SQLException {
        if (data == null) {
            ps.setNull(indice, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(indice, Timestamp.valueOf(data.withNano(0)));
        }
    }

    static boolean lerSimNao(ResultSet rs, String coluna) throws SQLException {
        return SIM.equalsIgnoreCase(rs.getString(coluna));
    }

    static String simNao(boolean valor) {
        return valor ? SIM : NAO;
    }

    /** Coordenadas NUMBER(9,6): arredonda para 6 casas decimais. */
    static void definirCoordenada(PreparedStatement ps, int indice, double valor) throws SQLException {
        ps.setBigDecimal(indice, BigDecimal.valueOf(valor).setScale(6, RoundingMode.HALF_UP));
    }

    static void definirTexto(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, valor);
        }
    }

    /** "?, ?, ?" com a quantidade de marcadores pedida (para clausulas IN). */
    static String marcadores(int quantidade) {
        return String.join(", ", Collections.nCopies(quantidade, "?"));
    }

    static void definirIds(PreparedStatement ps, int primeiroIndice, Collection<Long> ids) throws SQLException {
        int i = primeiroIndice;
        for (Long id : ids) {
            ps.setLong(i++, id);
        }
    }

    // ------------------------------------------------------------------
    // Sequences
    // ------------------------------------------------------------------

    /** Proximo valor da sequence (nomes fixos definidos nos DAOs, nunca vindos do usuario). */
    static long proximoId(Connection conn, String sequence) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT " + sequence + ".NEXTVAL FROM DUAL");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    // ------------------------------------------------------------------
    // Transacoes
    // ------------------------------------------------------------------

    @FunctionalInterface
    interface Trabalho<T> {
        T executar(Connection conn) throws SQLException;
    }

    /**
     * Executa o trabalho numa unica transacao: setAutoCommit(false), commit
     * no final e rollback em qualquer erro. Erros do Oracle sao traduzidos
     * pelo {@link TradutorErroOracle}.
     */
    static <T> T emTransacao(ConnectionFactory factory, String operacao, Trabalho<T> trabalho) {
        try (Connection conn = factory.getConnection()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                T resultado = trabalho.executar(conn);
                conn.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                desfazer(conn, e);
                throw TradutorErroOracle.traduzir(e, operacao);
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        } catch (SQLException e) {
            throw TradutorErroOracle.traduzir(e, operacao);
        }
    }

    private static void desfazer(Connection conn, Exception causa) {
        try {
            conn.rollback();
        } catch (SQLException erroRollback) {
            causa.addSuppressed(erroRollback);
        }
    }
}
