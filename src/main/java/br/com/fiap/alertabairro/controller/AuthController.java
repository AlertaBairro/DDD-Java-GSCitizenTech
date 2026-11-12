package br.com.fiap.alertabairro.controller;

import br.com.fiap.alertabairro.bo.UsuarioBO;
import br.com.fiap.alertabairro.dto.LoginRequest;
import br.com.fiap.alertabairro.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login de usuários cadastrados")
public class AuthController {

    private final UsuarioBO usuarioBO;

    public AuthController(UsuarioBO usuarioBO) {
        this.usuarioBO = usuarioBO;
    }

    @PostMapping("/login")
    @Operation(summary = "Login com e-mail e senha (público)",
            description = "Só autentica usuários existentes e ativos. Devolve id, nome, perfil e bairro; "
                    + "envie o id no header X-Usuario-Id nas demais requisições.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(usuarioBO.login(req));
    }
}
