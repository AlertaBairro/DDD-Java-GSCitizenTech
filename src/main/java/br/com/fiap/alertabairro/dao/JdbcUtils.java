package br.com.fiap.alertabairro.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Funcoes auxiliares de JDBC compartilhadas pelos DAOs
 * (conversao de datas com fuso horario e de valores opcionais).
 */
final class JdbcUtils {

    static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private JdbcUtils() {
    }

    static OffsetDateTime lerData(ResultSet rs, String coluna) throws SQLException {
        OffsetDateTime data = rs.getObject(coluna, OffsetDateTime.class);
        return data == null ? null : data.atZoneSameInstant(FUSO).toOffsetDateTime();
    }

    static void definirData(PreparedStatement ps, int indice, OffsetDateTime data) throws SQLException {
        if (data == null) {
            ps.setNull(indice, Types.TIMESTAMP_WITH_TIMEZONE);
        } else {
            ps.setObject(indice, data, Types.TIMESTAMP_WITH_TIMEZONE);
        }
    }

    static Double lerDouble(ResultSet rs, String coluna) throws SQLException {
        BigDecimal valor = rs.getBigDecimal(coluna);
        return valor == null ? null : valor.doubleValue();
    }

    static void definirDouble(PreparedStatement ps, int indice, Double valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.NUMERIC);
        } else {
            ps.setBigDecimal(indice, BigDecimal.valueOf(valor));
        }
    }

    static void definirTexto(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, valor);
        }
    }
}
