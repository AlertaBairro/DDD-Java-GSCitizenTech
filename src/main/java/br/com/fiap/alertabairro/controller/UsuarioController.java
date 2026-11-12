package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.UsuarioBO;
import br.com.fiap.alertabairro.dto.UsuarioCadastroRequest;
import br.com.fiap.alertabairro.dto.UsuarioResponse;
import br.com.fiap.alertabairro.dto.UsuarioUpdateRequest;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Cadastro (público) e manutenção da própria conta")
public class UsuarioController {

    private final UsuarioBO usuarioBO;

    public UsuarioController(UsuarioBO usuarioBO) {
        this.usuarioBO = usuarioBO;
    }

    @PostMapping
    @Operation(summary = "Cadastra um usuário (público)",
            description = "Nome, e-mail único, senha (gravada em BCrypt), telefone opcional, bairro e perfil.")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioCadastroRequest req) {
        UsuarioResponse criado = usuarioBO.cadastrar(req);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta a própria conta")
    public ResponseEntity<UsuarioResponse> buscar(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(usuarioBO.buscar(id, usuarioId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome, telefone, bairro e (opcionalmente) a senha da própria conta")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader("X-Usuario-Id") Long usuarioId,
            @Valid @RequestBody UsuarioUpdateRequest req) {
        return ResponseEntity.ok(usuarioBO.atualizar(id, usuarioId, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui a própria conta (exclusão lógica: st_ativo = 'N')")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @Parameter(description = "Id do usuário logado") @RequestHeader("X-Usuario-Id") Long usuarioId) {
        usuarioBO.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
