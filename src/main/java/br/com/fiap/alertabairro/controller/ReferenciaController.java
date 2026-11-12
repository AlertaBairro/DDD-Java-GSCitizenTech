package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.ReferenciaBO;
import br.com.fiap.alertabairro.dto.ReferenciasResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referencias")
@Tag(name = "Referências", description = "Bairros, tipos, status, perfis e ponto de referência do mapa")
public class ReferenciaController {

    private final ReferenciaBO referenciaBO;

    public ReferenciaController(ReferenciaBO referenciaBO) {
        this.referenciaBO = referenciaBO;
    }

    @GetMapping
    @Operation(summary = "Dados de referência (público)")
    public ResponseEntity<ReferenciasResponse> listar() {
        return ResponseEntity.ok(referenciaBO.obterReferencias());
    }
}
