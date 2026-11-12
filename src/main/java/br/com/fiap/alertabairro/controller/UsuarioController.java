package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.UsuarioBO;
import br.com.fiap.alertabairro.dto.UsuarioResponse;
import br.com.fiap.alertabairro.dto.UsuarioUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Consulta e manutenção da própria conta")
public class UsuarioController {

    private final UsuarioBO usuarioBO;

    public UsuarioController(UsuarioBO usuarioBO) {
        this.usuarioBO = usuarioBO;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dados de um usuário")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable Long id,
                                                  @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(usuarioBO.buscar(id, usuarioId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar nome e/ou senha da própria conta")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody @Valid UsuarioUpdateRequest request,
                                                     @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(usuarioBO.atualizar(id, request, usuarioId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir a própria conta",
            description = "Remove a conta, os apoios dados e as ocorrências registradas pelo usuário.")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @RequestHeader("X-Usuario-Id") Long usuarioId) {
        usuarioBO.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
