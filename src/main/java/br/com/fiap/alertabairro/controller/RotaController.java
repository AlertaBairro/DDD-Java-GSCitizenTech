package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.RotaBO;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaRequest;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rotas")
@Tag(name = "Rota segura", description = "Avaliação de rotas candidatas contra as zonas de risco")
public class RotaController {

    private final RotaBO rotaBO;

    public RotaController(RotaBO rotaBO) {
        this.rotaBO = rotaBO;
    }

    @PostMapping("/avaliar")
    @Operation(summary = "Avaliar rotas candidatas",
            description = "Para cada rota, conta as zonas de risco atravessadas (distância até a polilinha <= 40 m) "
                    + "e indica a mais segura: menos zonas e, no empate, menor duração.")
    public ResponseEntity<AvaliacaoRotaResponse> avaliar(@RequestBody @Valid AvaliacaoRotaRequest request,
                                                         @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(rotaBO.avaliar(request, usuarioId));
    }
}
