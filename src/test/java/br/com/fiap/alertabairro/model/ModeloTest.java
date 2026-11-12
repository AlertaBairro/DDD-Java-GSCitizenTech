package br.com.fiap.alertabairro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ModeloTest {

    private static final LocalDateTime REGISTRO = LocalDateTime.of(2026, 11, 10, 8, 0);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 12, 10, 0);

    @Test
    @DisplayName("Ocorrência nasce ABERTA e sem data de resolução")
    void ocorrenciaNasceAberta() {
        Ocorrencia o = new Ocorrencia();
        assertThat(o.getStatus()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(o.isAberta()).isTrue();
        assertThat(o.getDataResolucao()).isNull();
    }

    @Test
    @DisplayName("Ir para RESOLVIDA preenche a data de resolução; sair de RESOLVIDA limpa")
    void alterarStatusRespeitaCheckDeResolucao() {
        Ocorrencia o = new Ocorrencia();
        o.setDataRegistro(REGISTRO);

        o.alterarStatus(StatusOcorrencia.RESOLVIDA, AGORA);
        assertThat(o.isResolvida()).isTrue();
        assertThat(o.getDataResolucao()).isEqualTo(AGORA);

        o.alterarStatus(StatusOcorrencia.EM_ATENDIMENTO, AGORA);
        assertThat(o.getDataResolucao()).isNull();
    }

    @Test
    @DisplayName("Data de resolução nunca fica antes da data de registro")
    void resolucaoNaoAntecedeRegistro() {
        Ocorrencia o = new Ocorrencia();
        o.setDataRegistro(AGORA);
        o.alterarStatus(StatusOcorrencia.RESOLVIDA, REGISTRO);
        assertThat(o.getDataResolucao()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("Encerrar ponto alagado desativa e grava a data de encerramento")
    void encerrarPontoAlagado() {
        PontoAlagado p = new PontoAlagado();
        p.setDataReporte(REGISTRO);
        assertThat(p.isAtivo()).isTrue();

        p.encerrar(AGORA);
        assertThat(p.isAtivo()).isFalse();
        assertThat(p.getDataEncerramento()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("Raio do alerta por nível da água: 300, 500, 600 e 800 m")
    void raioPorNivelDaAgua() {
        assertThat(NivelAgua.TORNOZELO.getRaioAlertaMetros()).isEqualTo(300);
        assertThat(NivelAgua.JOELHO.getRaioAlertaMetros()).isEqualTo(500);
        assertThat(NivelAgua.CINTURA.getRaioAlertaMetros()).isEqualTo(600);
        assertThat(NivelAgua.INTRANSITAVEL.getRaioAlertaMetros()).isEqualTo(800);
    }

    @Test
    @DisplayName("Alerta do usuário: marcar como lido grava a data de leitura")
    void marcarAlertaComoLido() {
        AlertaUsuario au = new AlertaUsuario(1L, 2L, REGISTRO, null);
        assertThat(au.isLido()).isFalse();
        au.marcarComoLido(AGORA);
        assertThat(au.isLido()).isTrue();
        assertThat(au.getDataLeitura()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("Autoria da ocorrência é verificada pelo id do usuário")
    void autoria() {
        Ocorrencia o = new Ocorrencia();
        o.setIdUsuario(7L);
        assertThat(o.foiRegistradaPor(7L)).isTrue();
        assertThat(o.foiRegistradaPor(8L)).isFalse();
    }
}
