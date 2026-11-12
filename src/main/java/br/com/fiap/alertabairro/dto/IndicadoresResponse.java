package br.com.fiap.alertabairro.dto;

import br.com.fiap.alertabairro.dao.IndicadorDAO.ReincidenciaBairroTipo;
import br.com.fiap.alertabairro.dao.IndicadorDAO.ReincidenciaPonto;
import br.com.fiap.alertabairro.dao.IndicadorDAO.ResumoBairro;
import br.com.fiap.alertabairro.dao.IndicadorDAO.ResumoTipo;

import java.util.List;

/**
 * Painel do gestor da subprefeitura.
 *
 * @param tempoMedioResolucaoHoras media entre registro e resolucao (null se nada foi resolvido)
 * @param taxaLeituraPercentual    alertas lidos / alertas entregues, em %
 */
public record IndicadoresResponse(
        long totalOcorrencias,
        long ocorrenciasPendentes,
        long ocorrenciasResolvidas,
        Double tempoMedioResolucaoHoras,
        String tempoMedioResolucaoTexto,
        List<ResumoBairro> porBairro,
        List<ResumoTipo> porTipo,
        long pontosAlagadosAtivos,
        long pontosAlagadosTotal,
        long alertasEnviados,
        long notificacoesEntregues,
        long usuariosAlertados,
        long notificacoesLidas,
        double taxaLeituraPercentual,
        List<ReincidenciaPonto> reincidenciaPontosAlagados,
        List<ReincidenciaBairroTipo> reincidenciaBairroTipo) {
}
