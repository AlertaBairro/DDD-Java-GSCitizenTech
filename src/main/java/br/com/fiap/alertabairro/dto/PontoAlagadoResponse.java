package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.NivelAgua;

import java.time.LocalDateTime;

/** Ponto alagado (T_AB_PONTO_ALAGADO). */
public record PontoAlagadoResponse(
        Long id,
        ResumoResponse bairro,
        ResumoResponse reportadoPor,
        String enderecoReferencia,
        double latitude,
        double longitude,
        NivelAgua nivelAgua,
        String nivelAguaDescricao,
        LocalDateTime dataReporte,
        boolean ativo,
        LocalDateTime dataEncerramento) {
}
