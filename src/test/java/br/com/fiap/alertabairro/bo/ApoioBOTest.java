package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dao.HistoricoStatusDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.ConflitoException;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.model.Apoio;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApoioBOTest {

    private OcorrenciaDAO ocorrenciaDAO;
    private ApoioDAO apoioDAO;
    private ApoioBO bo;

    @BeforeEach
    void preparar() {
        ocorrenciaDAO = mock(OcorrenciaDAO.class);
        apoioDAO = mock(ApoioDAO.class);
        var usuarioDAO = Dados.usuarioDAO();
        var bairroDAO = Dados.bairroDAO();
        UsuarioBO usuarioBO = Dados.usuarioBO(usuarioDAO, bairroDAO);
        when(apoioDAO.listarIdsOcorrenciasApoiadas(anyLong())).thenReturn(Set.of());
        OcorrenciaBO ocorrenciaBO = new OcorrenciaBO(ocorrenciaDAO, apoioDAO, mock(HistoricoStatusDAO.class),
                bairroDAO, Dados.tipoDAO(), usuarioDAO, usuarioBO, new RiscoBO(Dados.RELOGIO));
        bo = new ApoioBO(apoioDAO, ocorrenciaBO, usuarioBO, Dados.RELOGIO);
    }

    private void ocorrencia(Long autor, StatusOcorrencia status) {
        Ocorrencia o = Dados.ocorrencia(5L, autor, status, 3, 1, Dados.AGORA.minusDays(1));
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));
    }

    @Test
    @DisplayName("Apoiar: grava o apoio do morador com a data atual")
    void apoiar() {
        ocorrencia(Dados.OUTRO_MORADOR, StatusOcorrencia.ABERTA);
        bo.apoiar(5L, Dados.MORADOR);

        ArgumentCaptor<Apoio> captor = ArgumentCaptor.forClass(Apoio.class);
        verify(apoioDAO).inserir(captor.capture());
        assertThat(captor.getValue().getIdOcorrencia()).isEqualTo(5L);
        assertThat(captor.getValue().getIdUsuario()).isEqualTo(Dados.MORADOR);
        assertThat(captor.getValue().getDataApoio()).isEqualTo(Dados.AGORA);
    }

    @Test
    @DisplayName("Apoiar a própria ocorrência dá 422")
    void apoiarPropria() {
        ocorrencia(Dados.MORADOR, StatusOcorrencia.ABERTA);
        assertThatThrownBy(() -> bo.apoiar(5L, Dados.MORADOR))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("própria");
        verify(apoioDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Apoiar ocorrência RESOLVIDA dá 422")
    void apoiarResolvida() {
        ocorrencia(Dados.OUTRO_MORADOR, StatusOcorrencia.RESOLVIDA);
        assertThatThrownBy(() -> bo.apoiar(5L, Dados.MORADOR)).isInstanceOf(RegraDeNegocioException.class);
        verify(apoioDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Apoiar de novo dá 409 (um apoio por usuário)")
    void apoioDuplicado() {
        ocorrencia(Dados.OUTRO_MORADOR, StatusOcorrencia.ABERTA);
        when(apoioDAO.existe(5L, Dados.MORADOR)).thenReturn(true);
        assertThatThrownBy(() -> bo.apoiar(5L, Dados.MORADOR)).isInstanceOf(ConflitoException.class);
        verify(apoioDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Só MORADOR apoia (403 para agente)")
    void apoioSomenteMorador() {
        ocorrencia(Dados.OUTRO_MORADOR, StatusOcorrencia.ABERTA);
        assertThatThrownBy(() -> bo.apoiar(5L, Dados.AGENTE)).isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("Retirar apoio inexistente dá 404; existente remove a linha")
    void retirarApoio() {
        ocorrencia(Dados.OUTRO_MORADOR, StatusOcorrencia.ABERTA);
        when(apoioDAO.excluir(5L, Dados.MORADOR)).thenReturn(false);
        assertThatThrownBy(() -> bo.retirar(5L, Dados.MORADOR)).isInstanceOf(RecursoNaoEncontradoException.class);

        when(apoioDAO.excluir(5L, Dados.MORADOR)).thenReturn(true);
        bo.retirar(5L, Dados.MORADOR);
        verify(apoioDAO, org.mockito.Mockito.times(2)).excluir(5L, Dados.MORADOR);
    }
}
