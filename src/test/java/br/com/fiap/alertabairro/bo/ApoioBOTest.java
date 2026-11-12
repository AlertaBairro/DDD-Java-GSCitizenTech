package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dao.TipoProblemaDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.OcorrenciaResponse;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.ConflitoException;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import br.com.fiap.alertabairro.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static br.com.fiap.alertabairro.bo.Dados.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ApoioBO - apoiar e retirar apoio")
class ApoioBOTest {

    @Mock private ApoioDAO apoioDAO;
    @Mock private OcorrenciaDAO ocorrenciaDAO;
    @Mock private BairroDAO bairroDAO;
    @Mock private TipoProblemaDAO tipoProblemaDAO;
    @Mock private UsuarioDAO usuarioDAO;
    @Mock private PasswordEncoder passwordEncoder;

    private ApoioBO apoioBO;

    private final Usuario autor = morador(10);
    private final Usuario apoiador = morador(11);
    private final Usuario agente = agente(20);

    @BeforeEach
    void configurar() {
        UsuarioBO usuarioBO = new UsuarioBO(usuarioDAO, apoioDAO, ocorrenciaDAO, passwordEncoder);
        OcorrenciaBO ocorrenciaBO = new OcorrenciaBO(ocorrenciaDAO, apoioDAO, bairroDAO, tipoProblemaDAO,
                usuarioBO, new RiscoBO(RELOGIO), RELOGIO);
        apoioBO = new ApoioBO(apoioDAO, ocorrenciaDAO, ocorrenciaBO, usuarioBO, RELOGIO);

        when(usuarioDAO.buscarPorId(10L)).thenReturn(Optional.of(autor));
        when(usuarioDAO.buscarPorId(11L)).thenReturn(Optional.of(apoiador));
        when(usuarioDAO.buscarPorId(20L)).thenReturn(Optional.of(agente));
    }

    private Ocorrencia ocorrenciaCom(StatusOcorrencia status, int apoios) {
        Ocorrencia o = ocorrencia(5, SANTANA, BUEIRO, autor, 10, apoios, status);
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));
        return o;
    }

    @Test
    @DisplayName("apoiar soma 1 ao contador e marca apoiadaPorMim")
    void apoiarSomaUm() {
        ocorrenciaCom(StatusOcorrencia.RECEBIDA, 3);
        when(apoioDAO.existe(11L, 5L)).thenReturn(false);

        OcorrenciaResponse resposta = apoioBO.apoiar(5L, 11L);

        assertThat(resposta.apoios()).isEqualTo(4);
        assertThat(resposta.apoiadaPorMim()).isTrue();
        verify(apoioDAO).inserir(any());
        verify(ocorrenciaDAO).alterarQuantidadeApoios(5L, 1);
    }

    @Test
    @DisplayName("apoio duplicado gera conflito (409)")
    void apoioDuplicado() {
        ocorrenciaCom(StatusOcorrencia.RECEBIDA, 3);
        when(apoioDAO.existe(11L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> apoioBO.apoiar(5L, 11L))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("já apoiou");
        verify(apoioDAO, never()).inserir(any());
        verify(ocorrenciaDAO, never()).alterarQuantidadeApoios(anyLong(), anyInt());
    }

    @Test
    @DisplayName("não é possível apoiar ocorrência RESOLVIDA (422)")
    void apoioEmResolvida() {
        ocorrenciaCom(StatusOcorrencia.RESOLVIDA, 3);

        assertThatThrownBy(() -> apoioBO.apoiar(5L, 11L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("resolvida");
        verify(apoioDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("agente não pode apoiar (403)")
    void agenteNaoApoia() {
        ocorrenciaCom(StatusOcorrencia.RECEBIDA, 3);

        assertThatThrownBy(() -> apoioBO.apoiar(5L, 20L))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("retirar apoio subtrai 1 do contador")
    void retirarSubtraiUm() {
        ocorrenciaCom(StatusOcorrencia.RECEBIDA, 4);
        when(apoioDAO.existe(11L, 5L)).thenReturn(true);

        OcorrenciaResponse resposta = apoioBO.retirar(5L, 11L);

        assertThat(resposta.apoios()).isEqualTo(3);
        assertThat(resposta.apoiadaPorMim()).isFalse();
        verify(apoioDAO).excluir(11L, 5L);
        verify(ocorrenciaDAO).alterarQuantidadeApoios(5L, -1);
    }

    @Test
    @DisplayName("retirar apoio que não existe gera 404")
    void retirarSemApoio() {
        ocorrenciaCom(StatusOcorrencia.RECEBIDA, 4);
        when(apoioDAO.existe(11L, 5L)).thenReturn(false);

        assertThatThrownBy(() -> apoioBO.retirar(5L, 11L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("autor não pode retirar o próprio apoio (422)")
    void autorNaoRetira() {
        ocorrenciaCom(StatusOcorrencia.RECEBIDA, 4);
        when(apoioDAO.existe(10L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> apoioBO.retirar(5L, 10L))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(apoioDAO, never()).excluir(anyLong(), anyLong());
    }
}
