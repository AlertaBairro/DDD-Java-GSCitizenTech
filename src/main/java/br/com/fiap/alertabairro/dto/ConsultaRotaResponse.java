package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.ConsultaRota;

import java.time.LocalDateTime;

/** Consulta de rota registrada (T_AB_CONSULTA_ROTA). */
public record ConsultaRotaResponse(
        Long id,
        String origem,
        double latitudeOrigem,
        double longitudeOrigem,
        String destino,
        double latitudeDestino,
        double longitudeDestino,
        LocalDateTime dataConsulta,
        boolean rotaCalculada,
        int pontosEvitados) {

    public static ConsultaRotaResponse de(ConsultaRota c) {
        return new ConsultaRotaResponse(c.getId(), c.getOrigem(), c.getLatitudeOrigem(), c.getLongitudeOrigem(),
                c.getDestino(), c.getLatitudeDestino(), c.getLongitudeDestino(), c.getDataConsulta(),
                c.isRotaCalculada(), c.getPontosEvitados());
    }
}
