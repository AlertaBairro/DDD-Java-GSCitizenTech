package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.Zona;

/** Bairro (T_AB_BAIRRO). */
public record BairroResponse(Long id, String nome, Zona zona, String zonaDescricao, String subprefeitura) {

    public static BairroResponse de(Bairro b) {
        return new BairroResponse(b.getId(), b.getNome(), b.getZona(), b.getZona().getDescricao(),
                b.getSubprefeitura());
    }
}
