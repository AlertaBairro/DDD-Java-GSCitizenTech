package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dao.TipoProblemaDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.OcorrenciaRequest;
import br.com.fiap.alertabairro.dto.OcorrenciaResponse;
import br.com.fiap.alertabairro.dto.StatusRequest;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.NaoAutenticadoException;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.Apoio;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import br.com.fiap.alertabairro.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static br.com.fiap.alertabairro.bo.Dados.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OcorrenciaBO - registro, edição, exclusão e status")
class OcorrenciaBOTest {

    @Mock private OcorrenciaDAO ocorrenciaDAO;
    @Mock private ApoioDAO apoioDAO;
    @Mock private BairroDAO bairroDAO;
    @Mock private TipoProblemaDAO tipoProblemaDAO;
    @Mock private UsuarioDAO usuarioDAO;
    @Mock private PasswordEncoder passwordEncoder;

    private OcorrenciaBO ocorrenciaBO;

    private final Usuario morador = morador(10);
    private final Usuario outroMorador = morador(11);
    private final Usuario agente = agente(20);

    @BeforeEach
    void configurar() {
        UsuarioBO usuarioBO = new UsuarioBO(usuarioDAO, apoioDAO, ocorrenciaDAO, passwordEncoder);
        ocorrenciaBO = new OcorrenciaBO(ocorrenciaDAO, apoioDAO, bairroDAO, tipoProblemaDAO, usuarioBO,
                new RiscoBO(RELOGIO), RELOGIO);

        when(usuarioDAO.buscarPorId(10L)).thenReturn(Optional.of(morador));
        when(usuarioDAO.buscarPorId(11L)).thenReturn(Optional.of(outroMorador));
        when(usuarioDAO.buscarPorId(20L)).thenReturn(Optional.of(agente));
        when(bairroDAO.buscarPorId(2L)).thenReturn(Optional.of(JARDIM_SAO_PAULO));
        when(tipoProblemaDAO.buscarPorCodigo("ALAGADO")).thenReturn(Optional.of(ALAGADO));
        when(tipoProblemaDAO.buscarPorCodigo("BUEIRO")).thenReturn(Optional.of(BUEIRO));
        when(ocorrenciaDAO.inserir(any())).thenAnswer(inv -> {
            Ocorrencia o = inv.getArgument(0);
            o.setId(99L);
            return o;
        });
    }

    // ------------------------------------------------------------------
    // Registro
    // ------------------------------------------------------------------

    @Test
    @DisplayName("morador registra ocorrência: nasce RECEBIDA, com 1 apoio do autor e data do sistema")
    void registraOcorrencia() {
        OcorrenciaRequest req = new OcorrenciaRequest(2L, "alagado", "  Água na calçada  ", -23.49, -46.61);

        OcorrenciaResponse resposta = ocorrenciaBO.registrar(req, 10L);

        assertThat(resposta.id()).isEqualTo(99L);
        assertThat(resposta.status()).isEqualTo(StatusOcorrencia.RECEBIDA);
        assertThat(resposta.apoios()).isEqualTo(1);
        assertThat(resposta.apoiadaPorMim()).isTrue();
        assertThat(resposta.editavel()).isTrue();
        assertThat(resposta.dataCriacao()).isEqualTo(AGORA);
        assertThat(resposta.observacoes()).isEqualTo("Água na calçada");
        assertThat(resposta.tipo().codigo()).isEqualTo("ALAGADO");

        ArgumentCaptor<Apoio> apoio = ArgumentCaptor.forClass(Apoio.class);
        verify(apoioDAO).inserir(apoio.capture());
        assertThat(apoio.getValue().getUsuarioId()).isEqualTo(10L);
        assertThat(apoio.getValue().getOcorrenciaId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("agente não pode registrar ocorrência (403)")
    void agenteNaoRegistra() {
        OcorrenciaRequest req = new OcorrenciaRequest(2L, "ALAGADO", null, null, null);

        assertThatThrownBy(() -> ocorrenciaBO.registrar(req, 20L))
                .isInstanceOf(AcessoNegadoException.class);
        verify(ocorrenciaDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("usuário inexistente no header não é autenticado (401)")
    void usuarioInexistente() {
        when(usuarioDAO.buscarPorId(404L)).thenReturn(Optional.empty());
        OcorrenciaRequest req = new OcorrenciaRequest(2L, "ALAGADO", null, null, null);

        assertThatThrownBy(() -> ocorrenciaBO.registrar(req, 404L))
                .isInstanceOf(NaoAutenticadoException.class);
    }

    @Test
    @DisplayName("latitude fora de -90..90 é rejeitada (400)")
    void latitudeInvalida() {
        OcorrenciaRequest req = new OcorrenciaRequest(2L, "ALAGADO", null, -91.0, -46.6);

        assertThatThrownBy(() -> ocorrenciaBO.registrar(req, 10L))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("latitude");
    }

    @Test
    @DisplayName("longitude fora de -180..180 é rejeitada (400)")
    void longitudeInvalida() {
        OcorrenciaRequest req = new OcorrenciaRequest(2L, "ALAGADO", null, -23.4, 181.0);

        assertThatThrownBy(() -> ocorrenciaBO.registrar(req, 10L))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("longitude");
    }

    @Test
    @DisplayName("latitude sem longitude é rejeitada (400)")
    void localizacaoIncompleta() {
        OcorrenciaRequest req = new OcorrenciaRequest(2L, "ALAGADO", null, -23.4, null);

        assertThatThrownBy(() -> ocorrenciaBO.registrar(req, 10L))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("tipo de problema inexistente gera 404")
    void tipoInexistente() {
        when(tipoProblemaDAO.buscarPorCodigo("ENCHENTE")).thenReturn(Optional.empty());
        OcorrenciaRequest req = new OcorrenciaRequest(2L, "ENCHENTE", null, null, null);

        assertThatThrownBy(() -> ocorrenciaBO.registrar(req, 10L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ------------------------------------------------------------------
    // Edicao e exclusao
    // ------------------------------------------------------------------

    @Test
    @DisplayName("autor edita ocorrência RECEBIDA")
    void autorEdita() {
        Ocorrencia o = ocorrencia(5, SANTANA, ALAGADO, morador, 1, 1, StatusOcorrencia.RECEBIDA);
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));

        OcorrenciaResponse resposta = ocorrenciaBO.editar(5L,
                new OcorrenciaRequest(2L, "BUEIRO", "Atualizado", null, null), 10L);

        assertThat(resposta.bairro().nome()).isEqualTo("Jardim São Paulo");
        assertThat(resposta.tipo().codigo()).isEqualTo("BUEIRO");
        verify(ocorrenciaDAO).atualizar(o);
    }

    @Test
    @DisplayName("outro morador não pode editar (403)")
    void outroMoradorNaoEdita() {
        Ocorrencia o = ocorrencia(5, SANTANA, ALAGADO, morador, 1, 1, StatusOcorrencia.RECEBIDA);
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> ocorrenciaBO.editar(5L,
                new OcorrenciaRequest(2L, "BUEIRO", null, null, null), 11L))
                .isInstanceOf(AcessoNegadoException.class);
        verify(ocorrenciaDAO, never()).atualizar(any());
    }

    @Test
    @DisplayName("não edita ocorrência que já está EM_ATENDIMENTO (422)")
    void naoEditaForaDeRecebida() {
        Ocorrencia o = ocorrencia(5, SANTANA, ALAGADO, morador, 1, 1, StatusOcorrencia.EM_ATENDIMENTO);
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> ocorrenciaBO.editar(5L,
                new OcorrenciaRequest(2L, "BUEIRO", null, null, null), 10L))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("autor exclui ocorrência RECEBIDA junto com os apoios")
    void autorExclui() {
        Ocorrencia o = ocorrencia(5, SANTANA, ALAGADO, morador, 1, 3, StatusOcorrencia.RECEBIDA);
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));

        ocorrenciaBO.excluir(5L, 10L);

        verify(apoioDAO).excluirPorOcorrencia(5L);
        verify(ocorrenciaDAO).excluir(5L);
    }

    @Test
    @DisplayName("não exclui ocorrência RESOLVIDA (422)")
    void naoExcluiResolvida() {
        Ocorrencia o = ocorrencia(5, SANTANA, ALAGADO, morador, 1, 3, StatusOcorrencia.RESOLVIDA);
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> ocorrenciaBO.excluir(5L, 10L))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(ocorrenciaDAO, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("ocorrência inexistente gera 404")
    void ocorrenciaInexistente() {
        when(ocorrenciaDAO.buscarPorId(777L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ocorrenciaBO.buscar(777L, 10L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ------------------------------------------------------------------
    // Status
    // ------------------------------------------------------------------

    @Test
    @DisplayName("agente muda para RESOLVIDA e a data de resolução é gravada com o momento atual")
    void resolverGravaData() {
        Ocorrencia o = ocorrencia(5, SANTANA, ALAGADO, morador, 10, 3, StatusOcorrencia.EM_ATENDIMENTO);
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));

        OcorrenciaResponse resposta = ocorrenciaBO.alterarStatus(5L,
                new StatusRequest(StatusOcorrencia.RESOLVIDA), 20L);

        verify(ocorrenciaDAO).atualizarStatus(5L, StatusOcorrencia.RESOLVIDA, AGORA);
        assertThat(resposta.dataResolucao()).isEqualTo(AGORA);
        assertThat(resposta.risco().rotulo()).isEqualTo("Resolvido");
    }

    @Test
    @DisplayName("ao sair de RESOLVIDA a data de resolução é limpa")
    void reabrirLimpaData() {
        Ocorrencia o = ocorrencia(5, SANTANA, ALAGADO, morador, 10, 3, StatusOcorrencia.RESOLVIDA);
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));

        OcorrenciaResponse resposta = ocorrenciaBO.alterarStatus(5L,
                new StatusRequest(StatusOcorrencia.EM_ATENDIMENTO), 20L);

        verify(ocorrenciaDAO).atualizarStatus(eq(5L), eq(StatusOcorrencia.EM_ATENDIMENTO), isNull());
        assertThat(resposta.dataResolucao()).isNull();
        assertThat(resposta.status()).isEqualTo(StatusOcorrencia.EM_ATENDIMENTO);
    }

    @Test
    @DisplayName("morador não pode alterar status (403)")
    void moradorNaoAlteraStatus() {
        assertThatThrownBy(() -> ocorrenciaBO.alterarStatus(5L,
                new StatusRequest(StatusOcorrencia.RESOLVIDA), 10L))
                .isInstanceOf(AcessoNegadoException.class);
        verify(ocorrenciaDAO, never()).atualizarStatus(anyLong(), any(), any());
    }

    @Test
    @DisplayName("mudar para o mesmo status é rejeitado (422)")
    void mesmoStatus() {
        Ocorrencia o = ocorrencia(5, SANTANA, ALAGADO, morador, 10, 3, StatusOcorrencia.RECEBIDA);
        when(ocorrenciaDAO.buscarPorId(5L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> ocorrenciaBO.alterarStatus(5L,
                new StatusRequest(StatusOcorrencia.RECEBIDA), 20L))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ------------------------------------------------------------------
    // Listas
    // ------------------------------------------------------------------

    @Test
    @DisplayName("lista informa apoiadaPorMim conforme os apoios do usuário do header")
    void listaComApoiadaPorMim() {
        Ocorrencia a = ocorrencia(1, SANTANA, BUEIRO, outroMorador, 5, 2, StatusOcorrencia.RECEBIDA);
        Ocorrencia b = ocorrencia(2, SANTANA, LIXO, outroMorador, 6, 1, StatusOcorrencia.RECEBIDA);
        when(ocorrenciaDAO.listar(null, null, null)).thenReturn(List.of(a, b));
        when(apoioDAO.listarIdsOcorrenciasApoiadas(10L)).thenReturn(Set.of(2L));

        List<OcorrenciaResponse> lista = ocorrenciaBO.listar(null, null, null, 10L);

        assertThat(lista).extracting(OcorrenciaResponse::apoiadaPorMim).containsExactly(false, true);
    }

    @Test
    @DisplayName("lista priorizada é exclusiva do agente (403 para morador)")
    void priorizadaSomenteAgente() {
        assertThatThrownBy(() -> ocorrenciaBO.listarPriorizadas(null, null, null, 10L))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("alertas ativos trazem só ALAGADO não resolvido")
    void alertasAtivos() {
        Ocorrencia ativo = ocorrencia(1, JARDIM_SAO_PAULO, ALAGADO, outroMorador, 2, 6, StatusOcorrencia.RECEBIDA);
        Ocorrencia resolvido = ocorrencia(2, JARDIM_SAO_PAULO, ALAGADO, outroMorador, 2, 6, StatusOcorrencia.RESOLVIDA);
        when(ocorrenciaDAO.listarAlagamentosAtivos()).thenReturn(List.of(ativo, resolvido));

        List<OcorrenciaResponse> alertas = ocorrenciaBO.listarAlertasAtivos(10L);

        assertThat(alertas).extracting(OcorrenciaResponse::id).containsExactly(1L);
    }
}
