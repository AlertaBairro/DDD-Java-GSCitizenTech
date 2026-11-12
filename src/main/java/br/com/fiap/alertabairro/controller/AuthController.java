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
@Tag(name = "Autenticação", description = "Login simples por e-mail, senha e perfil")
public class AuthController {

    private final UsuarioBO usuarioBO;

    public AuthController(UsuarioBO usuarioBO) {
        this.usuarioBO = usuarioBO;
    }

    @PostMapping("/login")
    @Operation(summary = "Login",
            description = "Valida e-mail e senha. Se o e-mail não existir, cria o usuário com o perfil escolhido. "
                    + "Envie o id devolvido no header X-Usuario-Id nas demais requisições.")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(usuarioBO.login(request));
    }
}
