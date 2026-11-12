package br.com.fiap.alertabairro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credenciais de acesso. */
public record LoginRequest(
        @NotBlank(message = "informe o e-mail")
        @Email(message = "e-mail inválido")
        @Size(max = 120, message = "o e-mail deve ter no máximo 120 caracteres")
        String email,

        @NotBlank(message = "informe a senha")
        @Size(max = 72, message = "a senha deve ter no máximo 72 caracteres")
        String senha) {
}
