package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.RotaBO;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaRequest;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaResponse;
import br.com.fiap.alertabairro.dto.ConsultaRotaResponse;
import br.com.fiap.alertabairro.dto.ZonaRiscoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Rota segura", description = "Zonas de risco (40 m) e avaliação de rotas candidatas")
public class RotaController {

    private static final String HEADER = "X-Usuario-Id";

    private final RotaBO rotaBO;

    public RotaController(RotaBO rotaBO) {
        this.rotaBO = rotaBO;
    }

    @GetMapping("/zonas-de-risco")
    @Operation(summary = "Pontos alagados ativos, cada um com raio de desvio de 40 m")
    public ResponseEntity<List<ZonaRiscoResponse>> zonasDeRisco(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(rotaBO.listarZonasDeRisco(usuarioId));
    }

    @PostMapping("/rotas/avaliar")
    @Operation(summary = "Avalia rotas candidatas e grava a consulta (T_AB_CONSULTA_ROTA)",
            description = "Uma rota atravessa a zona se a distância do ponto alagado à polilinha for ≤ 40 m. "
                    + "Mais segura: menos zonas; no empate, menor duração. A primeira rota é a padrão; "
                    + "pontos evitados = zonas da padrão − zonas da segura.")
    public ResponseEntity<AvaliacaoRotaResponse> avaliar(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId,
            @Valid @RequestBody AvaliacaoRotaRequest req) {
        return ResponseEntity.ok(rotaBO.avaliar(req, usuarioId));
    }

    @GetMapping("/rotas/minhas")
    @Operation(summary = "Histórico de consultas de rota do usuário logado")
    public ResponseEntity<List<ConsultaRotaResponse>> minhas(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(rotaBO.listarMinhas(usuarioId));
    }
}
