package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.IndicadorDAO;
import br.com.fiap.alertabairro.dao.IndicadorDAO.TotaisAlertas;
import br.com.fiap.alertabairro.dao.IndicadorDAO.TotaisOcorrencias;
import br.com.fiap.alertabairro.dao.IndicadorDAO.TotaisPontosAlagados;
import br.com.fiap.alertabairro.dto.IndicadoresResponse;
import br.com.fiap.alertabairro.model.Perfil;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Painel do gestor da subprefeitura: totais e pendencias, tempo medio de
 * resolucao, indicadores por bairro (consulta 2 do DQL) e por tipo, pontos
 * alagados, alertas e taxa de leitura, e reincidencias.
 */
@Service
public class IndicadorBO {

    private final IndicadorDAO indicadorDAO;
    private final UsuarioBO usuarioBO;

    public IndicadorBO(IndicadorDAO indicadorDAO, UsuarioBO usuarioBO) {
        this.indicadorDAO = indicadorDAO;
        this.usuarioBO = usuarioBO;
    }

    public IndicadoresResponse obter(Long idUsuario) {
        usuarioBO.autenticarComPerfil(idUsuario, Perfil.GESTOR);

        TotaisOcorrencias ocorrencias = indicadorDAO.totaisOcorrencias();
        TotaisPontosAlagados pontos = indicadorDAO.totaisPontosAlagados();
        TotaisAlertas alertas = indicadorDAO.totaisAlertas();
        Double mediaHoras = arredondar(ocorrencias.mediaHorasResolucao());

        return new IndicadoresResponse(
                ocorrencias.total(),
                ocorrencias.pendentes(),
                ocorrencias.total() - ocorrencias.pendentes(),
                mediaHoras,
                formatarHoras(mediaHoras),
                indicadorDAO.porBairro(),
                indicadorDAO.porTipo(),
                pontos.ativos(),
                pontos.total(),
                alertas.alertasEnviados(),
                alertas.destinatarios(),
                alertas.usuariosAlertados(),
                alertas.lidos(),
                taxaLeitura(alertas.lidos(), alertas.destinatarios()),
                indicadorDAO.reincidenciaPontosAlagados(),
                indicadorDAO.reincidenciaBairroTipo());
    }

    /** Percentual de notificacoes lidas, com uma casa decimal (0 se nada foi entregue). */
    static double taxaLeitura(long lidos, long entregues) {
        if (entregues <= 0) {
            return 0.0;
        }
        return Math.round(lidos * 1000.0 / entregues) / 10.0;
    }

    /** Texto amigavel para o tempo medio: "45 min", "5,5 h" ou "3 d 4 h". */
    static String formatarHoras(Double horas) {
        if (horas == null) {
            return "Nenhuma ocorrência resolvida";
        }
        if (horas < 1) {
            return Math.round(horas * 60) + " min";
        }
        if (horas < 24) {
            return String.format(Locale.of("pt", "BR"), "%.1f h", horas);
        }
        long totalHoras = Math.round(horas);
        return (totalHoras / 24) + " d " + (totalHoras % 24) + " h";
    }

    private static Double arredondar(Double valor) {
        return valor == null ? null : Math.round(valor * 10.0) / 10.0;
    }
}
