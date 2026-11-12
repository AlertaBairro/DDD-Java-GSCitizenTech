package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.NivelAgua;

import java.time.LocalDateTime;

/**
 * Alerta de alagamento (T_AB_ALERTA) com os dados do ponto que o gerou.
 *
 * @param raioMetros raio de divulgacao do alerta (depende do nivel da agua)
 * @param pontoAtivo o ponto alagado ainda esta ativo
 */
public record AlertaResponse(
        Long id,
        Long pontoAlagadoId,
        String mensagem,
        int raioMetros,
        LocalDateTime dataEnvio,
        String enderecoReferencia,
        Double latitude,
        Double longitude,
        NivelAgua nivelAgua,
        boolean pontoAtivo) {
}
