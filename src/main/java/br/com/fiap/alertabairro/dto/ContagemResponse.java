package br.com.fiap.alertabairro.dto;

/**
 * Contagem de ocorrencias por um agrupamento (bairro, tipo ou status).
 */
public record ContagemResponse(String codigo, String nome, long total) {
}
