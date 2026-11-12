package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dao.HistoricoStatusDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.HistoricoStatusResponse;
import br.com.fiap.alertabairro.dto.OcorrenciaRequest;
import br.com.fiap.alertabairro.dto.OcorrenciaResponse;
import br.com.fiap.alertabairro.dto.StatusRequest;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.ConflitoException;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.model.HistoricoStatus;
import br.com.fiap.alertabairro.model.NivelRisco;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
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

class OcorrenciaBOTest {

    private OcorrenciaDAO ocorrenciaDAO;
    private ApoioDAO apoioDAO;
    private HistoricoStatusDAO historicoDAO;
    private UsuarioDAO usuarioDAO;
    private OcorrenciaBO bo;

    @BeforeEach
    void preparar() {
        ocorrenciaDAO = mock(OcorrenciaDAO.class);
        apoioDAO = mock(ApoioDAO.class);
        historicoDAO = mock(HistoricoStatusDAO.class);
        usuarioDAO = Dados.usuarioDAO();
        var bairroDAO = Dados.bairroDAO();
        when(apoioDAO.listarIdsOcorrenciasApoiadas(anyLong())).thenReturn(Set.of());
        when(ocorrenciaDAO.atualizar(any())).thenReturn(true);
        when(ocorrenciaDAO.excluir(anyLong())).thenReturn(true);
        when(ocorrenciaDAO.alterarStatus(any(), any())).thenReturn(true);
        when(ocorrenciaDAO.inserir(any())).thenAnswer(inv -> {
            Ocorrencia o = inv.getArgument(0);
            o.setId(100L); // o DAO real preenche o id pela sequence
            return o;
        });
        bo =new OcorrenciaBO(ocorrenciaDAO, apoioDAO, historicoDAO, bairroDAO, Dados.tipoDAO(), usuarioDAO,
                Dados.usuarioBO(usuarioDAO, bairroDAO), new RiscoBO(Dados.RELOGIO));
    }

    private OcorrenciaRequest pedido(Long tipo) {
        return new OcorrenciaRequest(tipo, 1L, " Av. Leôncio de Magalhães, 100 ", -23.4922, -46.6166,
                "Bueiro tomado por folhas", null);
    }

    private Ocorrencia existente(Long autor, StatusOcorrencia status) {
        Ocorrencia o = Dados.ocorrencia(7L, autor, status, 5, 2, Dados.AGORA.minusDays(2));
        when(ocorrenciaDAO.buscarPorId(7L)).thenReturn(Optional.of(o));
        return o;
    }

    // ------------------------------------------------------------------
    // Registro
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Registro: nasce ABERTA, risco = peso do tipo, sem apoio do autor")
    void registroNasceAberta() {
        OcorrenciaResponse r = bo.registrar(pedido(3L), Dados.MORADOR);

        ArgumentCaptor<Ocorrencia> captor = ArgumentCaptor.forClass(Ocorrencia.class);
        verify(ocorrenciaDAO).inserir(captor.capture());
        Ocorrencia gravada = captor.getValue();
        assertThat(gravada.getStatus()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(gravada.getNivelRisco()).isEqualTo(Dados.LIXO.getPesoRisco());
        assertThat(gravada.getIdUsuario()).isEqualTo(Dados.MORADOR);
        assertThat(gravada.getEnderecoReferencia()).isEqualTo("Av. Leôncio de Magalhães, 100");
        assertThat(gravada.getDataRegistro()).isEqualTo(Dados.AGORA);
        assertThat(gravada.getDataResolucao()).isNull();
        assertThat(gravada.getQuantidadeApoios()).isZero();
        verify(apoioDAO, never()).inserir(any());

        assertThat(r.minhaOcorrencia()).isTrue();
        assertThat(r.editavel()).isTrue();
        assertThat(r.apoiadaPorMim()).isFalse();
        assertThat(r.tipo().nome()).isEqualTo("Lixo acumulado");
    }

    @Test
    @DisplayName("Registro: só MORADOR registra (403 para agente)")
    void registroSomenteMorador() {
        assertThatThrownBy(() -> bo.registrar(pedido(1L), Dados.AGENTE)).isInstanceOf(AcessoNegadoException.class);
        verify(ocorrenciaDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Registro: tipo inexistente dá 422")
    void registroTipoInexistente() {
        assertThatThrownBy(() -> bo.registrar(pedido(99L), Dados.MORADOR))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("Tipo");
    }

    // ------------------------------------------------------------------
    // Edicao e exclusao
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Edição: autor edita enquanto ABERTA e o risco acompanha o novo tipo")
    void edicaoPeloAutor() {
        Ocorrencia o = existente(Dados.MORADOR, StatusOcorrencia.ABERTA);
        bo.editar(7L, pedido(3L), Dados.MORADOR);
        verify(ocorrenciaDAO).atualizar(o);
        assertThat(o.getIdTipoOcorrencia()).isEqualTo(3L);
        assertThat(o.getNivelRisco()).isEqualTo(3);
    }

    @Test
    @DisplayName("Edição: quem não é o autor recebe 403")
    void edicaoPorOutroUsuario() {
        existente(Dados.MORADOR, StatusOcorrencia.ABERTA);
        assertThatThrownBy(() -> bo.editar(7L, pedido(1L), Dados.OUTRO_MORADOR))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("Edição e exclusão só enquanto ABERTA (RECEBIDA dá 422)")
    void edicaoEExclusaoSomenteAberta() {
        existente(Dados.MORADOR, StatusOcorrencia.RECEBIDA);
        assertThatThrownBy(() -> bo.editar(7L, pedido(1L), Dados.MORADOR))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("ABERTA");
        assertThatThrownBy(() -> bo.excluir(7L, Dados.MORADOR))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("ABERTA");
        verify(ocorrenciaDAO, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("Exclusão pelo autor chama a exclusão transacional (apoios + histórico + ocorrência)")
    void exclusaoPeloAutor() {
        existente(Dados.MORADOR, StatusOcorrencia.ABERTA);
        bo.excluir(7L, Dados.MORADOR);
        verify(ocorrenciaDAO).excluir(7L);
    }

    @Test
    @DisplayName("Ocorrência inexistente dá 404")
    void ocorrenciaInexistente() {
        when(ocorrenciaDAO.buscarPorId(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bo.buscar(123L, Dados.MORADOR)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ------------------------------------------------------------------
    // Status + historico
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Status: agente muda o status e o histórico registra agente, anterior, novo e observação")
    void statusGravaHistorico() {
        Ocorrencia o = existente(Dados.MORADOR, StatusOcorrencia.ABERTA);

        OcorrenciaResponse r = bo.alterarStatus(7L, new StatusRequest(StatusOcorrencia.RECEBIDA, " Triada "),
                Dados.AGENTE);

        ArgumentCaptor<HistoricoStatus> captor = ArgumentCaptor.forClass(HistoricoStatus.class);
        verify(ocorrenciaDAO).alterarStatus(org.mockito.ArgumentMatchers.eq(o), captor.capture());
        HistoricoStatus h = captor.getValue();
        assertThat(h.getIdOcorrencia()).isEqualTo(7L);
        assertThat(h.getIdUsuario()).isEqualTo(Dados.AGENTE);
        assertThat(h.getStatusAnterior()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(h.getStatusNovo()).isEqualTo(StatusOcorrencia.RECEBIDA);
        assertThat(h.getObservacao()).isEqualTo("Triada");
        assertThat(h.getDataAlteracao()).isEqualTo(Dados.AGORA);
        assertThat(r.status()).isEqualTo(StatusOcorrencia.RECEBIDA);
        assertThat(r.dataResolucao()).isNull();
    }

    @Test
    @DisplayName("Status: ir para RESOLVIDA preenche a data de resolução")
    void statusResolvidaPreencheData() {
        Ocorrencia o = existente(Dados.MORADOR, StatusOcorrencia.EM_ATENDIMENTO);
        bo.alterarStatus(7L, new StatusRequest(StatusOcorrencia.RESOLVIDA, null), Dados.AGENTE);
        assertThat(o.getDataResolucao()).isEqualTo(Dados.AGORA);
    }

    @Test
    @DisplayName("Status: sair de RESOLVIDA limpa a data de resolução")
    void statusReabrirLimpaData() {
        Ocorrencia o = existente(Dados.MORADOR, StatusOcorrencia.RESOLVIDA);
        assertThat(o.getDataResolucao()).isNotNull();
        bo.alterarStatus(7L, new StatusRequest(StatusOcorrencia.EM_ATENDIMENTO, "Voltou a entupir"), Dados.AGENTE);
        assertThat(o.getDataResolucao()).isNull();
    }

    @Test
    @DisplayName("Status: não pode voltar para ABERTA (422)")
    void statusNaoVoltaParaAberta() {
        existente(Dados.MORADOR, StatusOcorrencia.RECEBIDA);
        assertThatThrownBy(() -> bo.alterarStatus(7L, new StatusRequest(StatusOcorrencia.ABERTA, null), Dados.AGENTE))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("ABERTA");
        verify(ocorrenciaDAO, never()).alterarStatus(any(), any());
    }

    @Test
    @DisplayName("Status: novo status igual ao atual dá 422")
    void statusIgualAoAtual() {
        existente(Dados.MORADOR, StatusOcorrencia.RECEBIDA);
        assertThatThrownBy(() -> bo.alterarStatus(7L, new StatusRequest(StatusOcorrencia.RECEBIDA, null), Dados.AGENTE))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(ocorrenciaDAO, never()).alterarStatus(any(), any());
    }

    @Test
    @DisplayName("Status: morador e gestor não alteram status (403)")
    void statusSomenteAgente() {
        existente(Dados.MORADOR, StatusOcorrencia.ABERTA);
        StatusRequest req = new StatusRequest(StatusOcorrencia.RECEBIDA, null);
        assertThatThrownBy(() -> bo.alterarStatus(7L, req, Dados.MORADOR)).isInstanceOf(AcessoNegadoException.class);
        assertThatThrownBy(() -> bo.alterarStatus(7L, req, Dados.GESTOR)).isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("Status: alteração concorrente (UPDATE sem linhas) dá 409")
    void statusConcorrente() {
        existente(Dados.MORADOR, StatusOcorrencia.ABERTA);
        when(ocorrenciaDAO.alterarStatus(any(), any())).thenReturn(false);
        assertThatThrownBy(() -> bo.alterarStatus(7L, new StatusRequest(StatusOcorrencia.RECEBIDA, null), Dados.AGENTE))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    @DisplayName("Histórico: lista as mudanças com o nome do agente")
    void historicoComNomeDoAgente() {
        existente(Dados.MORADOR, StatusOcorrencia.RECEBIDA);
        when(historicoDAO.listarPorOcorrencia(7L)).thenReturn(List.of(new HistoricoStatus(1L, 7L, Dados.AGENTE,
                StatusOcorrencia.ABERTA, StatusOcorrencia.RECEBIDA, Dados.AGORA, "Triada")));
        when(usuarioDAO.buscarNomes(any())).thenReturn(Map.of(Dados.AGENTE, "Agente de Teste"));

        List<HistoricoStatusResponse> historico = bo.historico(7L, Dados.MORADOR);

        assertThat(historico).hasSize(1);
        assertThat(historico.get(0).agente().nome()).isEqualTo("Agente de Teste");
        assertThat(historico.get(0).statusNovo()).isEqualTo(StatusOcorrencia.RECEBIDA);
    }

    // ------------------------------------------------------------------
    // Priorizacao
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Priorizadas: só AGENTE e em ordem de pontuação decrescente")
    void priorizadas() {
        Ocorrencia baixa = Dados.ocorrencia(1L, Dados.MORADOR, StatusOcorrencia.ABERTA, 2, 0, Dados.AGORA);
        Ocorrencia alta = Dados.ocorrencia(2L, Dados.MORADOR, StatusOcorrencia.ABERTA, 5, 4, Dados.AGORA.minusDays(3));
        Ocorrencia resolvida = Dados.ocorrencia(3L, Dados.MORADOR, StatusOcorrencia.RESOLVIDA, 5, 9,
                Dados.AGORA.minusDays(10));
        when(ocorrenciaDAO.listar(null, null, null)).thenReturn(List.of(baixa, resolvida, alta));

        List<OcorrenciaResponse> fila = bo.listarPriorizadas(Dados.AGENTE, null, null, null);

        assertThat(fila).extracting(OcorrenciaResponse::id).containsExactly(2L, 1L, 3L);
        assertThat(fila.get(0).prioridade().nivel()).isEqualTo(NivelRisco.ALTO);
        assertThat(fila.get(2).prioridade().pontuacao()).isZero();
        assertThatThrownBy(() -> bo.listarPriorizadas(Dados.MORADOR, null, null, null))
                .isInstanceOf(AcessoNegadoException.class);
    }
}
