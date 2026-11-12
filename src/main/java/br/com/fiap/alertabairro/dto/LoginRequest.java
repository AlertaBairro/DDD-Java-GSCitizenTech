package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados de login: e-mail, senha e perfil escolhido na tela inicial.
 */
public record LoginRequest(
        @Schema(example = "morador@alertabairro.com")
        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @Schema(example = "morador123")
        @NotBlank(message = "Informe a senha.")
        @Size(max = 72, message = "A senha deve ter no máximo 72 caracteres.")
        String senha,

        @Schema(example = "MORADOR")
        @NotNull(message = "Informe o perfil (MORADOR, AGENTE ou GESTOR).")
        Perfil perfil) {
}
