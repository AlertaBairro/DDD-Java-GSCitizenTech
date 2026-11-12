package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.HealthBO;
import br.com.fiap.alertabairro.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@Tag(name = "Health check", description = "Situação da API e da conexão com o banco")
public class HealthController {

    private final HealthBO healthBO;

    public HealthController(HealthBO healthBO) {
        this.healthBO = healthBO;
    }

    @GetMapping("/api/health")
    @Operation(summary = "Status da API e do banco (público)")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(healthBO.verificar());
    }

    /** A raiz da URL publica redireciona para a documentacao Swagger. */
    @Hidden
    @GetMapping("/")
    public RedirectView raiz() {
        return new RedirectView("/swagger-ui.html");
    }
}
