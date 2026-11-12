package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.Usuario;

/**
 * Resposta do login. O front guarda o id e o envia no header X-Usuario-Id.
 */
public record LoginResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        String perfilDescricao,
        boolean novoUsuario) {

    public static LoginResponse de(Usuario usuario, boolean novoUsuario) {
        return new LoginResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil(), usuario.getPerfil().getDescricao(), novoUsuario);
    }
}
