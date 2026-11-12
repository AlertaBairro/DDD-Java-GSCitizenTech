package br.com.fiap.alertabairro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Rotas candidatas a serem avaliadas quanto as zonas de risco de alagamento.
 */
public record AvaliacaoRotaRequest(
        @NotEmpty(message = "Informe ao menos uma rota candidata.")
        List<@Valid RotaCandidataRequest> rotas) {
}
