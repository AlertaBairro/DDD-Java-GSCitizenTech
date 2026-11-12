package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Usuario;

/**
 * Morador que registrou a ocorrencia.
 */
public record AutorResponse(Long id, String nome) {

    public static AutorResponse de(Usuario usuario) {
        return usuario == null ? null : new AutorResponse(usuario.getId(), usuario.getNome());
    }
}
