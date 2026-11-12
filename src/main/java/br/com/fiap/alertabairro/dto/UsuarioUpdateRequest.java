package br.com.fiap.alertabairro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Atualizacao da propria conta. Senha opcional: so muda se for informada. */
public record UsuarioUpdateRequest(
        @NotBlank(message = "informe o nome")
        @Size(max = 100, message = "o nome deve ter no máximo 100 caracteres")
        String nome,

        @Pattern(regexp = "^\\+?[0-9]{8,14}$", message = "telefone deve ter de 8 a 14 dígitos (somente números)")
        String telefone,

        @NotNull(message = "informe o bairro")
        Long bairroId,

        @Size(min = 4, max = 72, message = "a nova senha deve ter entre 4 e 72 caracteres")
        String senha) {
}
