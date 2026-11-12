package br.com.fiap.alertabairro.dto;

/**
 * Referencia curta a outro registro (bairro, tipo, autor, agente...).
 */
public record ResumoResponse(Long id, String nome) {
}
