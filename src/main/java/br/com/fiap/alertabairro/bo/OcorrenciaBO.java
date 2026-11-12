package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dao.TipoProblemaDAO;
import br.com.fiap.alertabairro.dto.OcorrenciaRequest;
import br.com.fiap.alertabairro.dto.OcorrenciaResponse;
import br.com.fiap.alertabairro.dto.StatusRequest;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.Apoio;
import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import br.com.fiap.alertabairro.model.TipoProblema;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Regras de negocio das ocorrencias: registro, edicao, exclusao, consulta,
 * alteracao de status, lista priorizada por risco e alertas de alagamento.
 */
@Service
public class OcorrenciaBO {

    private final OcorrenciaDAO ocorrenciaDAO;
    private final ApoioDAO apoioDAO;
    private final BairroDAO bairroDAO;
    private final TipoProblemaDAO tipoProblemaDAO;
    private final UsuarioBO usuarioBO;
    private final RiscoBO riscoBO;
    private final Clock clock;

    public OcorrenciaBO(OcorrenciaDAO ocorrenciaDAO, ApoioDAO apoioDAO, BairroDAO bairroDAO,
                        TipoProblemaDAO tipoProblemaDAO, UsuarioBO usuarioBO, RiscoBO riscoBO, Clock clock) {
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.apoioDAO = apoioDAO;
        this.bairroDAO = bairroDAO;
        this.tipoProblemaDAO = tipoProblemaDAO;
        this.usuarioBO = usuarioBO;
        this.riscoBO = riscoBO;
        this.clock = clock;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** Lista com filtros opcionais, das mais recentes para as mais antigas. */
    public List<OcorrenciaResponse> listar(Long bairroId, String tipo, StatusOcorrencia status, Long usuarioId) {
        Usuario usuario = usuarioBO.autenticar(usuarioId);
        List<Ocorrencia> ocorrencias = ocorrenciaDAO.listar(bairroId, normalizarFiltroTipo(tipo), status);
        return montarRespostas(ocorrencias, usuario.getId());
    }

    public OcorrenciaResponse buscar(Long id, Long usuarioId) {
        Usuario usuario = usuarioBO.autenticar(usuarioId);
        Ocorrencia ocorrencia = buscarEntidade(id);
        return montarResposta(ocorrencia, apoioDAO.existe(usuario.getId(), id), usuario.getId());
    }

    /** Lista do agente: ordenada por pontuacao de risco (decrescente) e, no empate, pela mais recente. */
    public List<OcorrenciaResponse> listarPriorizadas(Long bairroId, String tipo, StatusOcorrencia status,
                                                      Long usuarioId) {
        Usuario agente = usuarioBO.autenticarComPerfil(usuarioId, Perfil.AGENTE,
                "ver a lista priorizada de ocorrências");
        List<Ocorrencia> ocorrencias = ocorrenciaDAO.listar(bairroId, normalizarFiltroTipo(tipo), status);
        Set<Long> apoiadas = apoioDAO.listarIdsOcorrenciasApoiadas(agente.getId());
        return riscoBO.priorizar(ocorrencias).stream()
                .map(p -> OcorrenciaResponse.de(p.ocorrencia(), p.risco(),
                        apoiadas.contains(p.ocorrencia().getId()), agente.getId()))
                .toList();
    }

    /** Alertas de alagamento ativos: ALAGADO e nao RESOLVIDA, da mais recente para a mais antiga. */
    public List<OcorrenciaResponse> listarAlertasAtivos(Long usuarioId) {
        Usuario usuario = usuarioBO.autenticar(usuarioId);
        List<Ocorrencia> alertas = ocorrenciaDAO.listarAlagamentosAtivos().stream()
                .filter(o -> o.isAlagamento() && !o.isResolvida())
                .toList();
        return montarRespostas(alertas, usuario.getId());
    }

    // ------------------------------------------------------------------
    // Registro, edicao e exclusao (morador)
    // ------------------------------------------------------------------

    /**
     * Registra uma ocorrencia (somente MORADOR). Nasce RECEBIDA, com data/hora
     * do sistema e 1 apoio, que e de quem registrou.
     */
    public OcorrenciaResponse registrar(OcorrenciaRequest request, Long usuarioId) {
        Usuario morador = usuarioBO.autenticarComPerfil(usuarioId, Perfil.MORADOR, "registrar ocorrências");
        validarRequisicao(request);

        Ocorrencia ocorrencia = new Ocorrencia();
        aplicarDados(ocorrencia, request);
        ocorrencia.setUsuario(morador);
        OffsetDateTime agora = OffsetDateTime.now(clock);
        ocorrencia.setDataCriacao(agora);
        ocorrencia.setQuantidadeApoios(1);
        ocorrencia.setStatus(StatusOcorrencia.RECEBIDA);
        ocorrencia.setDataResolucao(null);

        ocorrenciaDAO.inserir(ocorrencia);
        apoioDAO.inserir(new Apoio(null, morador.getId(), ocorrencia.getId(), agora));
        return montarResposta(ocorrencia, true, morador.getId());
    }

    /** Edita bairro, tipo, observacoes e localizacao (somente o autor e enquanto RECEBIDA). */
    public OcorrenciaResponse editar(Long id, OcorrenciaRequest request, Long usuarioId) {
        Usuario usuario = usuarioBO.autenticar(usuarioId);
        Ocorrencia ocorrencia = buscarEntidade(id);
        exigirAutorComStatusRecebida(ocorrencia, usuario, "editar", "editá-la");
        validarRequisicao(request);

        aplicarDados(ocorrencia, request);
        ocorrenciaDAO.atualizar(ocorrencia);
        return montarResposta(ocorrencia, apoioDAO.existe(usuario.getId(), id), usuario.getId());
    }

    /** Exclui a ocorrencia e seus apoios (somente o autor e enquanto RECEBIDA). */
    public void excluir(Long id, Long usuarioId) {
        Usuario usuario = usuarioBO.autenticar(usuarioId);
        Ocorrencia ocorrencia = buscarEntidade(id);
        exigirAutorComStatusRecebida(ocorrencia, usuario, "excluir", "excluí-la");

        apoioDAO.excluirPorOcorrencia(id);
        ocorrenciaDAO.excluir(id);
    }

    // ------------------------------------------------------------------
    // Status (agente)
    // ------------------------------------------------------------------

    /**
     * Altera o status (somente AGENTE). Ao ir para RESOLVIDA grava a data de
     * resolucao com o momento atual; ao sair de RESOLVIDA limpa essa data.
     */
    public OcorrenciaResponse alterarStatus(Long id, StatusRequest request, Long usuarioId) {
        Usuario agente = usuarioBO.autenticarComPerfil(usuarioId, Perfil.AGENTE, "alterar o status de ocorrências");
        if (request == null || request.status() == null) {
            throw new ValidacaoException("Informe o status (RECEBIDA, EM_ATENDIMENTO ou RESOLVIDA).");
        }
        Ocorrencia ocorrencia = buscarEntidade(id);
        StatusOcorrencia novoStatus = request.status();
        if (ocorrencia.getStatus() == novoStatus) {
            throw new RegraDeNegocioException("A ocorrência já está com o status " + novoStatus.getDescricao() + ".");
        }

        OffsetDateTime dataResolucao = novoStatus == StatusOcorrencia.RESOLVIDA ? OffsetDateTime.now(clock) : null;
        ocorrenciaDAO.atualizarStatus(id, novoStatus, dataResolucao);
        ocorrencia.setStatus(novoStatus);
        ocorrencia.setDataResolucao(dataResolucao);
        return montarResposta(ocorrencia, apoioDAO.existe(agente.getId(), id), agente.getId());
    }

    // ------------------------------------------------------------------
    // Auxiliares (tambem usados por outros BOs)
    // ------------------------------------------------------------------

    public Ocorrencia buscarEntidade(Long id) {
        return ocorrenciaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrência " + id + " não encontrada."));
    }

    public OcorrenciaResponse montarResposta(Ocorrencia ocorrencia, boolean apoiadaPorMim, Long usuarioId) {
        return OcorrenciaResponse.de(ocorrencia, riscoBO.calcular(ocorrencia), apoiadaPorMim, usuarioId);
    }

    private List<OcorrenciaResponse> montarRespostas(List<Ocorrencia> ocorrencias, Long usuarioId) {
        if (ocorrencias.isEmpty()) {
            return List.of();
        }
        Set<Long> apoiadas = apoioDAO.listarIdsOcorrenciasApoiadas(usuarioId);
        return ocorrencias.stream()
                .map(o -> montarResposta(o, apoiadas.contains(o.getId()), usuarioId))
                .toList();
    }

    private void exigirAutorComStatusRecebida(Ocorrencia ocorrencia, Usuario usuario, String acao,
                                              String acaoComPronome) {
        if (!ocorrencia.foiRegistradaPor(usuario.getId())) {
            throw new AcessoNegadoException("Somente o morador que registrou a ocorrência pode "
                    + acaoComPronome + ".");
        }
        if (!ocorrencia.isRecebida()) {
            throw new RegraDeNegocioException("Só é possível " + acao + " ocorrências com status Recebida. Status atual: "
                    + ocorrencia.getStatus().getDescricao() + ".");
        }
    }

    private void validarRequisicao(OcorrenciaRequest request) {
        if (request == null) {
            throw new ValidacaoException("Informe os dados da ocorrência.");
        }
        if (request.bairroId() == null) {
            throw new ValidacaoException("Informe o bairro.");
        }
        if (request.tipo() == null || request.tipo().isBlank()) {
            throw new ValidacaoException("Informe o tipo de problema.");
        }
        if (request.observacoes() != null
                && request.observacoes().trim().length() > Ocorrencia.TAMANHO_MAXIMO_OBSERVACOES) {
            throw new ValidacaoException("As observações devem ter no máximo "
                    + Ocorrencia.TAMANHO_MAXIMO_OBSERVACOES + " caracteres.");
        }
        validarLocalizacao(request.latitude(), request.longitude());
    }

    /** Localizacao opcional: se vier, latitude entre -90 e 90 e longitude entre -180 e 180. */
    void validarLocalizacao(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            throw new ValidacaoException("Informe latitude e longitude juntas, ou nenhuma das duas.");
        }
        if (latitude.isNaN() || latitude < -90 || latitude > 90) {
            throw new ValidacaoException("A latitude deve estar entre -90 e 90.");
        }
        if (longitude.isNaN() || longitude < -180 || longitude > 180) {
            throw new ValidacaoException("A longitude deve estar entre -180 e 180.");
        }
    }

    private void aplicarDados(Ocorrencia ocorrencia, OcorrenciaRequest request) {
        Bairro bairro = bairroDAO.buscarPorId(request.bairroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Bairro " + request.bairroId() + " não encontrado."));
        TipoProblema tipo = buscarTipo(request.tipo());
        String observacoes = request.observacoes() == null ? null : request.observacoes().trim();

        ocorrencia.setBairro(bairro);
        ocorrencia.setTipo(tipo);
        ocorrencia.setObservacoes(observacoes == null || observacoes.isEmpty() ? null : observacoes);
        ocorrencia.setLatitude(request.latitude());
        ocorrencia.setLongitude(request.longitude());
    }

    private TipoProblema buscarTipo(String codigo) {
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        return tipoProblemaDAO.buscarPorCodigo(normalizado)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tipo de problema " + normalizado + " não encontrado."));
    }

    private String normalizarFiltroTipo(String tipo) {
        return tipo == null || tipo.isBlank() ? null : tipo.trim().toUpperCase(Locale.ROOT);
    }
}
