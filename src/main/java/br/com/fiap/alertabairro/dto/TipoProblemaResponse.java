package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.model.TipoProblema;

public record TipoProblemaResponse(String codigo, String nome, int peso, boolean alagamento) {

    public static TipoProblemaResponse de(TipoProblema tipo) {
        return new TipoProblemaResponse(tipo.getCodigo(), tipo.getNome(), tipo.getPeso(), tipo.isAlagamento());
    }
}
