package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Perfil;

import java.time.LocalDateTime;

/** Dados do usuario (sem o hash da senha). */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        Perfil perfil,
        String perfilDescricao,
        ResumoResponse bairro,
        LocalDateTime dataCadastro,
        boolean ativo) {
}
