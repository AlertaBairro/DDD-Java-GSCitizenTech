package br.com.fiap.alertabairro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Pedido de avaliacao de rota segura. A primeira rota da lista e a rota
 * padrao (a sugerida pelo roteador); se a lista vier vazia, a consulta e
 * registrada como "rota nao calculada".
 */
public record AvaliacaoRotaRequest(
        @NotNull(message = "informe a origem")
        @Valid
        LocalRotaRequest origem,

        @NotNull(message = "informe o destino")
        @Valid
        LocalRotaRequest destino,

        List<RotaCandidataRequest> rotas) {
}
