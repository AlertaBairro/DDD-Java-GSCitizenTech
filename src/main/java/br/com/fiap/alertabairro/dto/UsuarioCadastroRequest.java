package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cadastro de usuario (POST /api/usuarios). */
public record UsuarioCadastroRequest(
        @NotBlank(message = "informe o nome")
        @Size(max = 100, message = "o nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "informe o e-mail")
        @Email(message = "e-mail inválido")
        @Size(max = 120, message = "o e-mail deve ter no máximo 120 caracteres")
        String email,

        @NotBlank(message = "informe a senha")
        @Size(min = 4, max = 72, message = "a senha deve ter entre 4 e 72 caracteres")
        String senha,

        @Pattern(regexp = "^\\+?[0-9]{8,14}$", message = "telefone deve ter de 8 a 14 dígitos (somente números)")
        String telefone,

        @NotNull(message = "informe o bairro")
        Long bairroId,

        @NotNull(message = "informe o perfil (MORADOR, AGENTE ou GESTOR)")
        Perfil perfil) {
}
