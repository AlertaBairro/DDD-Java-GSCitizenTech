package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.NivelAgua;

import java.time.LocalDateTime;

/**
 * Zona de risco para o calculo de rota: um ponto alagado ativo com raio de
 * desvio fixo de 40 m (diferente do raio de divulgacao do alerta).
 */
public record ZonaRiscoResponse(
        Long pontoAlagadoId,
        String enderecoReferencia,
        double latitude,
        double longitude,
        NivelAgua nivelAgua,
        int raioMetros,
        LocalDateTime dataReporte) {
}
