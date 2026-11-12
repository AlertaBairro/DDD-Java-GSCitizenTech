package br.com.fiap.alertabairro.dto;

import java.util.List;

/**
 * Indicadores de impacto exibidos ao gestor da subprefeitura.
 *
 * @param totalOcorrencias          total de ocorrencias registradas
 * @param totalEmAberto             ocorrencias com status diferente de RESOLVIDA
 * @param totalResolvidas           ocorrencias RESOLVIDAS
 * @param tempoMedioResolucaoHoras  media (em horas) entre registro e resolucao; null se nenhuma resolvida
 * @param tempoMedioResolucaoTexto  a mesma media em texto (ex.: "2 d 2 h")
 * @param totalAlertasAlagamento    total de ocorrencias ALAGADO
 * @param alertasAlagamentoAtivos   ocorrencias ALAGADO ainda nao resolvidas
 * @param porBairro                 contagem por bairro
 * @param porTipo                   contagem por tipo de problema
 * @param porStatus                 contagem por status
 * @param reincidencias             combinacoes bairro + tipo com mais de 1 ocorrencia
 */
public record IndicadoresResponse(
        long totalOcorrencias,
        long totalEmAberto,
        long totalResolvidas,
        Double tempoMedioResolucaoHoras,
        String tempoMedioResolucaoTexto,
        long totalAlertasAlagamento,
        long alertasAlagamentoAtivos,
        List<ContagemResponse> porBairro,
        List<ContagemResponse> porTipo,
        List<ContagemResponse> porStatus,
        List<ReincidenciaResponse> reincidencias) {
}
