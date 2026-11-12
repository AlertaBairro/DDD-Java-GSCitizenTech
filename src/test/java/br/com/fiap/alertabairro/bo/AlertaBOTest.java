package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.AlertaDAO;
import br.com.fiap.alertabairro.dao.AlertaUsuarioDAO;
import br.com.fiap.alertabairro.dao.PontoAlagadoDAO;
import br.com.fiap.alertabairro.dto.AlertaResponse;
import br.com.fiap.alertabairro.dto.MeuAlertaResponse;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.model.Alerta;
import br.com.fiap.alertabairro.model.AlertaUsuario;
import br.com.fiap.alertabairro.model.NivelAgua;
import br.com.fiap.alertabairro.model.PontoAlagado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertaBOTest {

    private AlertaDAO alertaDAO;
    private AlertaUsuarioDAO alertaUsuarioDAO;
    private PontoAlagadoDAO pontoDAO;
    private AlertaBO bo;

    private final PontoAlagado ponto = new PontoAlagado();
    private final Alerta alerta = new Alerta(6L, 6L, "Início de alagamento em frente à estação.", 500,
            Dados.AGORA.minusHours(16));

    @BeforeEach
    void preparar() {
        alertaDAO = mock(AlertaDAO.class);
        alertaUsuarioDAO = mock(AlertaUsuarioDAO.class);
        pontoDAO = mock(PontoAlagadoDAO.class);
        var usuarioDAO = Dados.usuarioDAO();
        bo = new AlertaBO(alertaDAO, alertaUsuarioDAO, pontoDAO, Dados.usuarioBO(usuarioDAO, Dados.bairroDAO()),
                Dados.RELOGIO);

        ponto.setId(6L);
        ponto.setEnderecoReferencia("Av. Leôncio de Magalhães, em frente à Estação Jardim São Paulo");
        ponto.setLatitude(-23.4922);
        ponto.setLongitude(-46.6166);
        ponto.setNivelAgua(NivelAgua.TORNOZELO);
        ponto.setAtivo(true);

        when(alertaDAO.buscarPorId(6L)).thenReturn(Optional.of(alerta));
        when(alertaDAO.buscarPorIds(any())).thenReturn(Map.of(6L, alerta));
        when(pontoDAO.buscarPorId(6L)).thenReturn(Optional.of(ponto));
        when(pontoDAO.buscarPorIds(any())).thenReturn(Map.of(6L, ponto));
        when(alertaUsuarioDAO.buscar(anyLong(), anyLong())).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Alertas ativos trazem os dados do ponto alagado")
    void alertasAtivos() {
        when(alertaDAO.listarAtivos()).thenReturn(List.of(alerta));
        List<AlertaResponse> ativos = bo.listarAtivos(Dados.MORADOR);
        assertThat(ativos).hasSize(1);
        assertThat(ativos.get(0).pontoAtivo()).isTrue();
        assertThat(ativos.get(0).nivelAgua()).isEqualTo(NivelAgua.TORNOZELO);
        assertThat(ativos.get(0).raioMetros()).isEqualTo(500);
    }

    @Test
    @DisplayName("Meus alertas: lidos e não lidos do usuário do header")
    void meusAlertas() {
        when(alertaUsuarioDAO.listarPorUsuario(Dados.MORADOR)).thenReturn(List.of(
                new AlertaUsuario(6L, Dados.MORADOR, Dados.AGORA.minusHours(16), null)));
        List<MeuAlertaResponse> meus = bo.listarMeus(Dados.MORADOR);
        assertThat(meus).hasSize(1);
        assertThat(meus.get(0).lido()).isFalse();
        assertThat(meus.get(0).alerta().mensagem()).contains("alagamento");
    }

    @Test
    @DisplayName("Marcar como lido grava a data de leitura")
    void marcarComoLido() {
        AlertaUsuario recebido = new AlertaUsuario(6L, Dados.MORADOR, Dados.AGORA.minusHours(16), null);
        when(alertaUsuarioDAO.buscar(6L, Dados.MORADOR)).thenReturn(Optional.of(recebido));

        MeuAlertaResponse r = bo.marcarComoLido(6L, Dados.MORADOR);

        assertThat(r.lido()).isTrue();
        assertThat(r.dataLeitura()).isEqualTo(Dados.AGORA);
        verify(alertaUsuarioDAO).registrarLeitura(recebido);
    }

    @Test
    @DisplayName("Marcar como lido é idempotente: não regrava a data de leitura")
    void marcarComoLidoIdempotente() {
        AlertaUsuario lido = new AlertaUsuario(6L, Dados.MORADOR, Dados.AGORA.minusHours(16), Dados.AGORA.minusHours(15));
        when(alertaUsuarioDAO.buscar(6L, Dados.MORADOR)).thenReturn(Optional.of(lido));

        MeuAlertaResponse r = bo.marcarComoLido(6L, Dados.MORADOR);

        assertThat(r.dataLeitura()).isEqualTo(Dados.AGORA.minusHours(15));
        verify(alertaUsuarioDAO, never()).registrarLeitura(any());
    }

    @Test
    @DisplayName("Alerta que o usuário não recebeu dá 404")
    void alertaNaoRecebido() {
        assertThatThrownBy(() -> bo.marcarComoLido(6L, Dados.OUTRO_MORADOR))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
