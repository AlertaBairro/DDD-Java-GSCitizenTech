package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.PontoAlagadoDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.PontoAlagadoRequest;
import br.com.fiap.alertabairro.dto.ReportePontoAlagadoResponse;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.model.Alerta;
import br.com.fiap.alertabairro.model.NivelAgua;
import br.com.fiap.alertabairro.model.PontoAlagado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PontoAlagadoBOTest {

    private static final String ESTACAO = "Av. Leôncio de Magalhães, em frente à Estação Jardim São Paulo";

    private PontoAlagadoDAO pontoDAO;
    private UsuarioDAO usuarioDAO;
    private PontoAlagadoBO bo;

    @BeforeEach
    void preparar() {
        pontoDAO = mock(PontoAlagadoDAO.class);
        usuarioDAO = Dados.usuarioDAO();
        var bairroDAO = Dados.bairroDAO();
        when(pontoDAO.reportar(any(), any(), anyCollection()))
                .thenAnswer(inv -> ((Collection<?>) inv.getArgument(2)).size());
        when(pontoDAO.encerrar(any())).thenReturn(true);
        when(pontoDAO.buscarPorId(anyLong())).thenReturn(Optional.empty());
        bo = new PontoAlagadoBO(pontoDAO, usuarioDAO, bairroDAO, Dados.usuarioBO(usuarioDAO, bairroDAO),
                Dados.RELOGIO);
    }

    private PontoAlagadoRequest pedido(NivelAgua nivel) {
        return new PontoAlagadoRequest(1L, ESTACAO, -23.4922, -46.6166, nivel);
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Collection<Long>> captorDestinatarios() {
        return ArgumentCaptor.forClass(Collection.class);
    }

    @Test
    @DisplayName("Reporte: cria ponto ativo, alerta e destinatários (usuários ativos do bairro, menos quem reportou)")
    void reporteGeraAlertaEDestinatarios() {
        when(usuarioDAO.listarIdsAtivosPorBairro(1L))
                .thenReturn(List.of(Dados.MORADOR, Dados.OUTRO_MORADOR, Dados.AGENTE));

        ReportePontoAlagadoResponse r = bo.reportar(pedido(NivelAgua.JOELHO), Dados.MORADOR);

        ArgumentCaptor<PontoAlagado> ponto = ArgumentCaptor.forClass(PontoAlagado.class);
        ArgumentCaptor<Alerta> alerta = ArgumentCaptor.forClass(Alerta.class);
        ArgumentCaptor<Collection<Long>> destinatarios = captorDestinatarios();
        verify(pontoDAO).reportar(ponto.capture(), alerta.capture(), destinatarios.capture());

        assertThat(ponto.getValue().isAtivo()).isTrue();
        assertThat(ponto.getValue().getDataEncerramento()).isNull();
        assertThat(ponto.getValue().getIdUsuario()).isEqualTo(Dados.MORADOR);
        assertThat(ponto.getValue().getDataReporte()).isEqualTo(Dados.AGORA);
        assertThat(alerta.getValue().getRaioMetros()).isEqualTo(500);
        assertThat(alerta.getValue().getMensagem()).contains(ESTACAO).contains("joelho");
        assertThat(alerta.getValue().getDataEnvio()).isEqualTo(Dados.AGORA);
        assertThat(destinatarios.getValue()).containsExactly(Dados.OUTRO_MORADOR, Dados.AGENTE);
        assertThat(r.usuariosNotificados()).isEqualTo(2);
        assertThat(r.alerta().raioMetros()).isEqualTo(500);
    }

    @ParameterizedTest(name = "{0} -> {1} m")
    @CsvSource({"TORNOZELO, 300", "JOELHO, 500", "CINTURA, 600", "INTRANSITAVEL, 800"})
    @DisplayName("Raio do alerta pelo nível da água")
    void raioPorNivel(NivelAgua nivel, int raio) {
        when(usuarioDAO.listarIdsAtivosPorBairro(1L)).thenReturn(List.of());
        bo.reportar(pedido(nivel), Dados.MORADOR);
        ArgumentCaptor<Alerta> alerta = ArgumentCaptor.forClass(Alerta.class);
        verify(pontoDAO).reportar(any(), alerta.capture(), anyCollection());
        assertThat(alerta.getValue().getRaioMetros()).isEqualTo(raio);
    }

    @Test
    @DisplayName("Mensagem: intransitável pede rota alternativa; demais pedem para evitar o trecho")
    void mensagemPorNivel() {
        assertThat(PontoAlagadoBO.gerarMensagem("Av. Mazzei, 700", NivelAgua.INTRANSITAVEL))
                .isEqualTo("Trecho intransitável por alagamento em Av. Mazzei, 700. Procure rota alternativa.");
        assertThat(PontoAlagadoBO.gerarMensagem("Rua Dr. Zuquim, 900", NivelAgua.TORNOZELO))
                .isEqualTo("Alagamento com água na altura do tornozelo em Rua Dr. Zuquim, 900. Evite o trecho.");
        assertThat(PontoAlagadoBO.gerarMensagem("x".repeat(400), NivelAgua.CINTURA)).hasSize(300);
    }

    @Test
    @DisplayName("Reporte: só MORADOR (403) e bairro precisa existir (422)")
    void reporteValidacoes() {
        assertThatThrownBy(() -> bo.reportar(pedido(NivelAgua.JOELHO), Dados.AGENTE))
                .isInstanceOf(AcessoNegadoException.class);
        assertThatThrownBy(() -> bo.reportar(new PontoAlagadoRequest(99L, ESTACAO, -23.49, -46.61, NivelAgua.JOELHO),
                Dados.MORADOR)).isInstanceOf(RegraDeNegocioException.class);
        verify(pontoDAO, never()).reportar(any(), any(), anyCollection());
    }

    private PontoAlagado pontoAtivo() {
        PontoAlagado p = new PontoAlagado();
        p.setId(6L);
        p.setIdUsuario(Dados.MORADOR);
        p.setIdBairro(1L);
        p.setEnderecoReferencia(ESTACAO);
        p.setNivelAgua(NivelAgua.TORNOZELO);
        p.setDataReporte(Dados.AGORA.minusHours(16));
        p.setAtivo(true);
        when(pontoDAO.buscarPorId(6L)).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    @DisplayName("Encerrar: agente e gestor encerram (st_ativo = 'N' e data de encerramento)")
    void encerrar() {
        PontoAlagado p = pontoAtivo();
        bo.encerrar(6L, Dados.GESTOR);
        assertThat(p.isAtivo()).isFalse();
        assertThat(p.getDataEncerramento()).isEqualTo(Dados.AGORA);
        verify(pontoDAO).encerrar(p);

        PontoAlagado outro = pontoAtivo();
        bo.encerrar(6L, Dados.AGENTE);
        assertThat(outro.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("Encerrar: morador não encerra (403) e ponto já encerrado dá 422")
    void encerrarRegras() {
        PontoAlagado p = pontoAtivo();
        assertThatThrownBy(() -> bo.encerrar(6L, Dados.MORADOR)).isInstanceOf(AcessoNegadoException.class);

        p.encerrar(Dados.AGORA);
        assertThatThrownBy(() -> bo.encerrar(6L, Dados.AGENTE)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> bo.encerrar(77L, Dados.AGENTE)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
