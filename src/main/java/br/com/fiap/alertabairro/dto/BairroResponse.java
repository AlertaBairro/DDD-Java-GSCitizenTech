package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Bairro;

public record BairroResponse(Long id, String nome) {

    public static BairroResponse de(Bairro bairro) {
        return new BairroResponse(bairro.getId(), bairro.getNome());
    }
}
