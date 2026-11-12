package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Perfil;

/**
 * Resultado do login. O id deve ser enviado no header X-Usuario-Id nas demais requisicoes.
 */
public record LoginResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        String perfilDescricao,
        ResumoResponse bairro,
        String mensagem) {
}
