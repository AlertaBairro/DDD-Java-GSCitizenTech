package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.ApoioBO;
import br.com.fiap.alertabairro.bo.OcorrenciaBO;
import br.com.fiap.alertabairro.dto.OcorrenciaRequest;
import br.com.fiap.alertabairro.dto.OcorrenciaResponse;
import br.com.fiap.alertabairro.dto.StatusRequest;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/ocorrencias")
@Tag(name = "Ocorrências", description = "Registro, consulta, apoio e atendimento das ocorrências de zeladoria")
public class OcorrenciaController {

    private final OcorrenciaBO ocorrenciaBO;
    private final ApoioBO apoioBO;

    public OcorrenciaController(OcorrenciaBO ocorrenciaBO, ApoioBO apoioBO) {
        this.ocorrenciaBO = ocorrenciaBO;
        this.apoioBO = apoioBO;
    }

    // ------------------------------------------------------------------
    // GET
    // ------------------------------------------------------------------

    @GetMapping
    @Operation(summary = "Listar ocorrências",
            description = "Filtros opcionais por bairro (id), tipo (código) e status. Ordem: mais recentes primeiro.")
    public ResponseEntity<List<OcorrenciaResponse>> listar(
            @Parameter(description = "Id do bairro") @RequestParam(required = false) Long bairro,
            @Parameter(description = "Código do tipo (ALAGADO, BUEIRO, BOCA, LIXO)") @RequestParam(required = false) String tipo,
            @Parameter(description = "Status (RECEBIDA, EM_ATENDIMENTO, RESOLVIDA)") @RequestParam(required = false) StatusOcorrencia status,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(ocorrenciaBO.listar(bairro, tipo, status, usuarioId));
    }

    @GetMapping("/priorizadas")
    @Operation(summary = "Lista priorizada por risco (AGENTE)",
            description = "Ordenada por pontuação de risco decrescente e, no empate, pela mais recente.")
    public ResponseEntity<List<OcorrenciaResponse>> listarPriorizadas(
            @RequestParam(required = false) Long bairro,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) StatusOcorrencia status,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(ocorrenciaBO.listarPriorizadas(bairro, tipo, status, usuarioId));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Detalhar ocorrência")
    public ResponseEntity<OcorrenciaResponse> buscar(@PathVariable Long id,
                                                     @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(ocorrenciaBO.buscar(id, usuarioId));
    }

    // ------------------------------------------------------------------
    // POST
    // ------------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Registrar ocorrência (MORADOR)",
            description = "Nasce com status RECEBIDA e 1 apoio, de quem registrou.")
    public ResponseEntity<OcorrenciaResponse> registrar(@RequestBody @Valid OcorrenciaRequest request,
                                                        @RequestHeader("X-Usuario-Id") Long usuarioId) {
        OcorrenciaResponse criada = ocorrenciaBO.registrar(request, usuarioId);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @PostMapping("/{id:\\d+}/apoios")
    @Operation(summary = "Apoiar ocorrência (MORADOR)",
            description = "409 se o usuário já apoiou; 422 se a ocorrência está resolvida.")
    public ResponseEntity<OcorrenciaResponse> apoiar(@PathVariable Long id,
                                                     @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.status(201).body(apoioBO.apoiar(id, usuarioId));
    }

    // ------------------------------------------------------------------
    // PUT
    // ------------------------------------------------------------------

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Editar ocorrência (autor, enquanto RECEBIDA)")
    public ResponseEntity<OcorrenciaResponse> editar(@PathVariable Long id,
                                                     @RequestBody @Valid OcorrenciaRequest request,
                                                     @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(ocorrenciaBO.editar(id, request, usuarioId));
    }

    @PutMapping("/{id:\\d+}/status")
    @Operation(summary = "Alterar status (AGENTE)",
            description = "Ao ir para RESOLVIDA grava a data de resolução; ao sair de RESOLVIDA, limpa.")
    public ResponseEntity<OcorrenciaResponse> alterarStatus(@PathVariable Long id,
                                                            @RequestBody @Valid StatusRequest request,
                                                            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(ocorrenciaBO.alterarStatus(id, request, usuarioId));
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Excluir ocorrência (autor, enquanto RECEBIDA)",
            description = "Os apoios da ocorrência são removidos junto.")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        ocorrenciaBO.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id:\\d+}/apoios")
    @Operation(summary = "Retirar apoio (MORADOR)")
    public ResponseEntity<OcorrenciaResponse> retirarApoio(@PathVariable Long id,
                                                           @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(apoioBO.retirar(id, usuarioId));
    }
}
