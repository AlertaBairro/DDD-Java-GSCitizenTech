package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.PontoAlagadoBO;
import br.com.fiap.alertabairro.dto.PontoAlagadoRequest;
import br.com.fiap.alertabairro.dto.PontoAlagadoResponse;
import br.com.fiap.alertabairro.dto.ReportePontoAlagadoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pontos-alagados")
@Tag(name = "Pontos alagados", description = "Reporte de alagamento (gera alerta) e encerramento")
public class PontoAlagadoController {

    private static final String HEADER = "X-Usuario-Id";

    private final PontoAlagadoBO pontoAlagadoBO;

    public PontoAlagadoController(PontoAlagadoBO pontoAlagadoBO) {
        this.pontoAlagadoBO = pontoAlagadoBO;
    }

    @GetMapping
    @Operation(summary = "Lista os pontos alagados (todos, ou só os ativos com ?ativos=true)")
    public ResponseEntity<List<PontoAlagadoResponse>> listar(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId,
            @Parameter(description = "true = somente ativos") @RequestParam(defaultValue = "false") boolean ativos) {
        return ResponseEntity.ok(pontoAlagadoBO.listar(usuarioId, ativos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um ponto alagado pelo id")
    public ResponseEntity<PontoAlagadoResponse> buscar(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(pontoAlagadoBO.buscar(id, usuarioId));
    }

    @PostMapping
    @Operation(summary = "Reporta um ponto alagado (MORADOR)",
            description = "Na mesma transação: cria o ponto ativo, um alerta com raio pelo nível da água "
                    + "(TORNOZELO 300 m, JOELHO 500 m, CINTURA 600 m, INTRANSITAVEL 800 m) e notifica os "
                    + "usuários ativos do bairro, exceto quem reportou.")
    public ResponseEntity<ReportePontoAlagadoResponse> reportar(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId,
            @Valid @RequestBody PontoAlagadoRequest req) {
        ReportePontoAlagadoResponse criado = pontoAlagadoBO.reportar(req, usuarioId);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.ponto().id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @PutMapping("/{id}/encerrar")
    @Operation(summary = "Encerra o ponto alagado (AGENTE ou GESTOR)")
    public ResponseEntity<PontoAlagadoResponse> encerrar(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(pontoAlagadoBO.encerrar(id, usuarioId));
    }
}
