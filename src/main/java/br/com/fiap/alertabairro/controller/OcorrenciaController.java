package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.ApoioBO;
import br.com.fiap.alertabairro.bo.OcorrenciaBO;
import br.com.fiap.alertabairro.dto.HistoricoStatusResponse;
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
@Tag(name = "Ocorrências", description = "Zeladoria: registro, apoio, priorização, status e histórico")
public class OcorrenciaController {

    private static final String HEADER = "X-Usuario-Id";

    private final OcorrenciaBO ocorrenciaBO;
    private final ApoioBO apoioBO;

    public OcorrenciaController(OcorrenciaBO ocorrenciaBO, ApoioBO apoioBO) {
        this.ocorrenciaBO = ocorrenciaBO;
        this.apoioBO = apoioBO;
    }

    @GetMapping
    @Operation(summary = "Lista as ocorrências, com filtros opcionais por bairro, tipo e status")
    public ResponseEntity<List<OcorrenciaResponse>> listar(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId,
            @Parameter(description = "Id do bairro") @RequestParam(required = false) Long bairro,
            @Parameter(description = "Id do tipo de ocorrência") @RequestParam(required = false) Long tipo,
            @Parameter(description = "ABERTA, RECEBIDA, EM_ATENDIMENTO ou RESOLVIDA")
            @RequestParam(required = false) StatusOcorrencia status) {
        return ResponseEntity.ok(ocorrenciaBO.listar(usuarioId, bairro, tipo, status));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Busca uma ocorrência pelo id")
    public ResponseEntity<OcorrenciaResponse> buscar(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(ocorrenciaBO.buscar(id, usuarioId));
    }

    @PostMapping
    @Operation(summary = "Registra uma ocorrência (MORADOR)",
            description = "Nasce ABERTA, com nível de risco igual ao peso do tipo. O autor não apoia a própria ocorrência.")
    public ResponseEntity<OcorrenciaResponse> registrar(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId,
            @Valid @RequestBody OcorrenciaRequest req) {
        OcorrenciaResponse criada = ocorrenciaBO.registrar(req, usuarioId);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Edita a ocorrência (somente o autor e enquanto ABERTA)")
    public ResponseEntity<OcorrenciaResponse> editar(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId,
            @Valid @RequestBody OcorrenciaRequest req) {
        return ResponseEntity.ok(ocorrenciaBO.editar(id, req, usuarioId));
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Exclui a ocorrência (somente o autor e enquanto ABERTA)",
            description = "Remove antes os apoios e o histórico, na mesma transação.")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        ocorrenciaBO.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id:\\d+}/apoios")
    @Operation(summary = "Apoia a ocorrência (MORADOR; uma vez, nunca a própria nem uma RESOLVIDA)")
    public ResponseEntity<OcorrenciaResponse> apoiar(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(apoioBO.apoiar(id, usuarioId));
    }

    @DeleteMapping("/{id:\\d+}/apoios")
    @Operation(summary = "Retira o apoio dado à ocorrência (MORADOR)")
    public ResponseEntity<OcorrenciaResponse> retirarApoio(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(apoioBO.retirar(id, usuarioId));
    }

    @GetMapping("/priorizadas")
    @Operation(summary = "Fila priorizada do agente (AGENTE)",
            description = "pontuação = nivelRisco × 2 + apoios + dias × 0,8 (resolvida = 0); "
                    + "ALTO ≥ 16, MÉDIO ≥ 10. Ordem decrescente; no empate, a mais recente.")
    public ResponseEntity<List<OcorrenciaResponse>> priorizadas(
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId,
            @RequestParam(required = false) Long bairro,
            @RequestParam(required = false) Long tipo,
            @RequestParam(required = false) StatusOcorrencia status) {
        return ResponseEntity.ok(ocorrenciaBO.listarPriorizadas(usuarioId, bairro, tipo, status));
    }

    @PutMapping("/{id:\\d+}/status")
    @Operation(summary = "Altera o status (AGENTE) e grava o histórico",
            description = "O novo status deve ser diferente do atual e não pode ser ABERTA. "
                    + "RESOLVIDA preenche a data de resolução; sair de RESOLVIDA a limpa.")
    public ResponseEntity<OcorrenciaResponse> alterarStatus(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId,
            @Valid @RequestBody StatusRequest req) {
        return ResponseEntity.ok(ocorrenciaBO.alterarStatus(id, req, usuarioId));
    }

    @GetMapping("/{id:\\d+}/historico")
    @Operation(summary = "Histórico de mudanças de status da ocorrência (T_AB_HISTORICO_STATUS)")
    public ResponseEntity<List<HistoricoStatusResponse>> historico(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader(HEADER) Long usuarioId) {
        return ResponseEntity.ok(ocorrenciaBO.historico(id, usuarioId));
    }
}
