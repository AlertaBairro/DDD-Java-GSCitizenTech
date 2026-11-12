package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.AlertaBO;
import br.com.fiap.alertabairro.dto.AlertaResponse;
import br.com.fiap.alertabairro.dto.MeuAlertaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Alertas de alagamento gerados pelos pontos alagados")
public class AlertaController {

    private static final String HEADER = "X-Usuario-Id";

    private final AlertaBO alertaBO;

    public AlertaController(AlertaBO alertaBO) {
        this.alertaBO = alertaBO;
    }

    @GetMapping
    @Operation(summary = "Alertas ativos (de pontos alagados ainda ativos)")
    public ResponseEntity<List<AlertaResponse>> ativos(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(alertaBO.listarAtivos(usuarioId));
    }

    @GetMapping("/meus")
    @Operation(summary = "Alertas recebidos pelo usuário logado (lidos e não lidos)")
    public ResponseEntity<List<MeuAlertaResponse>> meus(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(alertaBO.listarMeus(usuarioId));
    }

    @PutMapping("/{id}/leitura")
    @Operation(summary = "Marca um alerta recebido como lido (grava dt_leitura)")
    public ResponseEntity<MeuAlertaResponse> marcarComoLido(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(alertaBO.marcarComoLido(id, usuarioId));
    }
}
