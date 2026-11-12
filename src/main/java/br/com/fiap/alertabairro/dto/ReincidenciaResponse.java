package br.com.fiap.alertabairro.dto;

/**
 * Combinacao bairro + tipo de problema com mais de uma ocorrencia.
 */
public record ReincidenciaResponse(Long bairroId, String bairro, String tipo, String tipoNome, long total) {
}
