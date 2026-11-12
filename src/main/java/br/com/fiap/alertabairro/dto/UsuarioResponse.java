package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.Usuario;

import java.time.OffsetDateTime;

/**
 * Dados publicos de um usuario (sem o hash da senha).
 */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        String perfilDescricao,
        OffsetDateTime dataCadastro) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil(), usuario.getPerfil().getDescricao(), usuario.getDataCadastro());
    }
}
