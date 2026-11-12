package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.IndicadorBO;
import br.com.fiap.alertabairro.dto.IndicadoresResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indicadores")
@Tag(name = "Indicadores", description = "Painel do gestor da subprefeitura")
public class IndicadorController {

    private final IndicadorBO indicadorBO;

    public IndicadorController(IndicadorBO indicadorBO) {
        this.indicadorBO = indicadorBO;
    }

    @GetMapping
    @Operation(summary = "Indicadores de zeladoria, alagamentos, alertas e reincidência (GESTOR)")
    public ResponseEntity<IndicadoresResponse> obter(
            @Parameter(description = "Id do usuário logado") @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(indicadorBO.obter(usuarioId));
    }
}
