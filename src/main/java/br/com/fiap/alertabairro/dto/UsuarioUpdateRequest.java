package br.com.fiap.alertabairro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Atualizacao da propria conta: novo nome e/ou nova senha (ao menos um deles).
 */
public record UsuarioUpdateRequest(
        @Schema(example = "Maria da Silva")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres.")
        String nome,

        @Schema(example = "novaSenha123")
        @Size(min = 4, max = 72, message = "A senha deve ter entre 4 e 72 caracteres.")
        String senha) {
}
