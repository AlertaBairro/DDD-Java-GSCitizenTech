package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.OcorrenciaBO;
import br.com.fiap.alertabairro.bo.RotaBO;
import br.com.fiap.alertabairro.dto.OcorrenciaResponse;
import br.com.fiap.alertabairro.dto.ZonaRiscoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Alertas de alagamento", description = "Alertas ativos e zonas de risco para o mapa")
public class AlertaController {

    private final OcorrenciaBO ocorrenciaBO;
    private final RotaBO rotaBO;

    public AlertaController(OcorrenciaBO ocorrenciaBO, RotaBO rotaBO) {
        this.ocorrenciaBO = ocorrenciaBO;
        this.rotaBO = rotaBO;
    }

    @GetMapping("/alertas")
    @Operation(summary = "Alertas de alagamento ativos",
            description = "Ocorrências ALAGADO não resolvidas, da mais recente para a mais antiga.")
    public ResponseEntity<List<OcorrenciaResponse>> alertas(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(ocorrenciaBO.listarAlertasAtivos(usuarioId));
    }

    @GetMapping("/zonas-de-risco")
    @Operation(summary = "Zonas de risco",
            description = "Pontos alagados ativos com localização, cada um com raio de 40 metros.")
    public ResponseEntity<List<ZonaRiscoResponse>> zonasDeRisco(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(rotaBO.listarZonasDeRisco(usuarioId));
    }
}
