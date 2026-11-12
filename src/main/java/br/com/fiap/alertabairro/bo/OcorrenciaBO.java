package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.bo.RiscoBO.OcorrenciaPriorizada;
import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.HistoricoStatusDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dao.TipoOcorrenciaDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.HistoricoStatusResponse;
import br.com.fiap.alertabairro.dto.OcorrenciaRequest;
import br.com.fiap.alertabairro.dto.OcorrenciaResponse;
import br.com.fiap.alertabairro.dto.ResumoResponse;
import br.com.fiap.alertabairro.dto.RiscoResponse;
import br.com.fiap.alertabairro.dto.StatusRequest;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.ConflitoException;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.HistoricoStatus;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import br.com.fiap.alertabairro.model.TipoOcorrencia;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Regras das ocorrencias de zeladoria:
 * <ul>
 *   <li>registro pelo MORADOR, nascendo ABERTA com nivelRisco = peso do tipo;</li>
 *   <li>edicao e exclusao somente pelo autor e enquanto ABERTA;</li>
 *   <li>mudanca de status pelo AGENTE, com historico, sem repetir o status
 *       atual e sem voltar para ABERTA;</li>
 *   <li>fila priorizada do agente (ver {@link RiscoBO}).</li>
 * </ul>
 */
@Service
public class OcorrenciaBO {

    private final OcorrenciaDAO ocorrenciaDAO;
    private final ApoioDAO apoioDAO;
    private final HistoricoStatusDAO historicoStatusDAO;
    private final BairroDAO bairroDAO;
    private final TipoOcorrenciaDAO tipoOcorrenciaDAO;
    private final UsuarioDAO usuarioDAO;
    private final UsuarioBO usuarioBO;
    private final RiscoBO riscoBO;

    public OcorrenciaBO(OcorrenciaDAO ocorrenciaDAO, ApoioDAO apoioDAO, HistoricoStatusDAO historicoStatusDAO,
                        BairroDAO bairroDAO, TipoOcorrenciaDAO tipoOcorrenciaDAO, UsuarioDAO usuarioDAO,
                        UsuarioBO usuarioBO, RiscoBO riscoBO) {
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.apoioDAO = apoioDAO;
        this.historicoStatusDAO = historicoStatusDAO;
        this.bairroDAO = bairroDAO;
        this.tipoOcorrenciaDAO = tipoOcorrenciaDAO;
        this.usuarioDAO = usuarioDAO;
        this.usuarioBO = usuarioBO;
        this.riscoBO = riscoBO;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public List<OcorrenciaResponse> listar(Long idUsuario, Long idBairro, Long idTipo, StatusOcorrencia status) {
        Usuario usuario = usuarioBO.autenticar(idUsuario);
        List<Ocorrencia> ocorrencias = ocorrenciaDAO.listar(idBairro, idTipo, status);
        return montarRespostas(riscoBO.avaliar(ocorrencias), usuario);
    }

    public OcorrenciaResponse buscar(Long id, Long idUsuario) {
        Usuario usuario = usuarioBO.autenticar(idUsuario);
        return montarResposta(buscarEntidade(id), usuario);
    }

    /** Fila do agente: pontuacao decrescente e, no empate, a mais recente primeiro. */
    public List<OcorrenciaResponse> listarPriorizadas(Long idUsuario, Long idBairro, Long idTipo,
                                                      StatusOcorrencia status) {
        Usuario agente = usuarioBO.autenticarComPerfil(idUsuario, Perfil.AGENTE);
        List<Ocorrencia> ocorrencias = ocorrenciaDAO.listar(idBairro, idTipo, status);
        return montarRespostas(riscoBO.priorizar(ocorrencias), agente);
    }

    public List<HistoricoStatusResponse> historico(Long id, Long idUsuario) {
        usuarioBO.autenticar(idUsuario);
        buscarEntidade(id);
        List<HistoricoStatus> historico = historicoStatusDAO.listarPorOcorrencia(id);
        Map<Long, String> nomes = usuarioDAO.buscarNomes(
                historico.stream().map(HistoricoStatus::getIdUsuario).collect(Collectors.toSet()));
        return historico.stream()
                .map(h -> new HistoricoStatusResponse(h.getId(), h.getIdOcorrencia(), h.getStatusAnterior(),
                        h.getStatusNovo(), new ResumoResponse(h.getIdUsuario(), nomes.get(h.getIdUsuario())),
                        h.getDataAlteracao(), h.getObservacao()))
                .toList();
    }

    // ------------------------------------------------------------------
    // Registro, edicao e exclusao (autor)
    // ------------------------------------------------------------------

    public OcorrenciaResponse registrar(OcorrenciaRequest req, Long idUsuario) {
        Usuario morador = usuarioBO.autenticarComPerfil(idUsuario, Perfil.MORADOR);
        validar(req);
        TipoOcorrencia tipo = exigirTipo(req.tipoId());
        Bairro bairro = exigirBairro(req.bairroId());

        Ocorrencia o = new Ocorrencia();
        o.setIdUsuario(morador.getId());
        preencher(o, req, tipo, bairro);
        o.setDataRegistro(riscoBO.agora());
        o.setStatus(StatusOcorrencia.ABERTA);
        o.setDataResolucao(null);
        o.setQuantidadeApoios(0); // o autor nao apoia a propria ocorrencia
        ocorrenciaDAO.inserir(o);
        return montarResposta(o, morador);
    }

    public OcorrenciaResponse editar(Long id, OcorrenciaRequest req, Long idUsuario) {
        Usuario usuario = usuarioBO.autenticar(idUsuario);
        Ocorrencia o = buscarEntidade(id);
        exigirAutorEAberta(o, usuario, "editar", "editada");
        validar(req);
        TipoOcorrencia tipo = exigirTipo(req.tipoId());
        Bairro bairro = exigirBairro(req.bairroId());

        preencher(o, req, tipo, bairro);
        if (!ocorrenciaDAO.atualizar(o)) {
            throw new RecursoNaoEncontradoException("Ocorrência não encontrada.");
        }
        return montarResposta(o, usuario);
    }

    /** Exclui a ocorrencia e, na mesma transacao, os apoios e o historico. */
    public void excluir(Long id, Long idUsuario) {
        Usuario usuario = usuarioBO.autenticar(idUsuario);
        Ocorrencia o = buscarEntidade(id);
        exigirAutorEAberta(o, usuario, "excluir", "excluída");
        if (!ocorrenciaDAO.excluir(id)) {
            throw new RecursoNaoEncontradoException("Ocorrência não encontrada.");
        }
    }

    // ------------------------------------------------------------------
    // Status (agente)
    // ------------------------------------------------------------------

    /**
     * Muda o status e grava o historico na mesma transacao. O novo status
     * precisa ser diferente do atual e nao pode ser ABERTA. Ao ir para
     * RESOLVIDA a data de resolucao e preenchida; ao sair, e limpa.
     */
    public OcorrenciaResponse alterarStatus(Long id, StatusRequest req, Long idUsuario) {
        Usuario agente = usuarioBO.autenticarComPerfil(idUsuario, Perfil.AGENTE);
        if (req == null || req.status() == null) {
            throw new ValidacaoException("Informe o novo status.");
        }
        Ocorrencia o = buscarEntidade(id);
        StatusOcorrencia anterior = o.getStatus();
        StatusOcorrencia novo = req.status();

        if (novo == StatusOcorrencia.ABERTA) {
            throw new RegraDeNegocioException("Não é possível voltar uma ocorrência para ABERTA.");
        }
        if (novo == anterior) {
            throw new RegraDeNegocioException("A ocorrência já está com o status " + novo + ".");
        }

        LocalDateTime agora = riscoBO.agora();
        o.alterarStatus(novo, agora);
        HistoricoStatus historico = new HistoricoStatus(null, o.getId(), agente.getId(), anterior, novo, agora,
                UsuarioBO.textoOpcional(req.observacao()));
        if (!ocorrenciaDAO.alterarStatus(o, historico)) {
            throw new ConflitoException("O status desta ocorrência foi alterado por outra pessoa. "
                    + "Atualize a lista e tente novamente.");
        }
        return montarResposta(o, agente);
    }

    // ------------------------------------------------------------------
    // Auxiliares usados tambem pelo ApoioBO
    // ------------------------------------------------------------------

    public Ocorrencia buscarEntidade(Long id) {
        return ocorrenciaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrência não encontrada."));
    }

    public OcorrenciaResponse montarResposta(Ocorrencia o, Usuario usuario) {
        return montarRespostas(List.of(new OcorrenciaPriorizada(o, riscoBO.calcular(o))), usuario).get(0);
    }

    List<OcorrenciaResponse> montarRespostas(List<OcorrenciaPriorizada> itens, Usuario usuario) {
        if (itens.isEmpty()) {
            return List.of();
        }
        Map<Long, String> bairros = bairroDAO.listar().stream()
                .collect(Collectors.toMap(Bairro::getId, Bairro::getNome));
        Map<Long, String> tipos = tipoOcorrenciaDAO.listar().stream()
                .collect(Collectors.toMap(TipoOcorrencia::getId, TipoOcorrencia::getNome));
        Set<Long> idsAutores = new HashSet<>();
        itens.forEach(i -> idsAutores.add(i.ocorrencia().getIdUsuario()));
        Map<Long, String> autores = usuarioDAO.buscarNomes(idsAutores);
        Set<Long> apoiadas = apoioDAO.listarIdsOcorrenciasApoiadas(usuario.getId());

        Function<OcorrenciaPriorizada, OcorrenciaResponse> mapear = item -> {
            Ocorrencia o = item.ocorrencia();
            boolean minha = o.foiRegistradaPor(usuario.getId());
            return new OcorrenciaResponse(
                    o.getId(),
                    new ResumoResponse(o.getIdTipoOcorrencia(), tipos.get(o.getIdTipoOcorrencia())),
                    new ResumoResponse(o.getIdBairro(), bairros.get(o.getIdBairro())),
                    new ResumoResponse(o.getIdUsuario(), autores.get(o.getIdUsuario())),
                    o.getDescricao(),
                    o.getEnderecoReferencia(),
                    o.getLatitude(),
                    o.getLongitude(),
                    o.getUrlFoto(),
                    o.getDataRegistro(),
                    o.getNivelRisco(),
                    o.getStatus(),
                    o.getStatus().getDescricao(),
                    o.getDataResolucao(),
                    o.getQuantidadeApoios(),
                    o.getId() != null && apoiadas.contains(o.getId()),
                    minha,
                    minha && o.isAberta(),
                    RiscoResponse.de(item.risco()));
        };
        return itens.stream().map(mapear).toList();
    }

    private void exigirAutorEAberta(Ocorrencia o, Usuario usuario, String acao, String participio) {
        if (!o.foiRegistradaPor(usuario.getId())) {
            throw new AcessoNegadoException("Apenas o autor pode " + acao + " a ocorrência.");
        }
        if (!o.isAberta()) {
            throw new RegraDeNegocioException("A ocorrência só pode ser " + participio
                    + " enquanto estiver ABERTA (status atual: " + o.getStatus() + ").");
        }
    }

    private void preencher(Ocorrencia o, OcorrenciaRequest req, TipoOcorrencia tipo, Bairro bairro) {
        o.setIdTipoOcorrencia(tipo.getId());
        o.setIdBairro(bairro.getId());
        o.setEnderecoReferencia(req.enderecoReferencia().trim());
        o.setLatitude(req.latitude());
        o.setLongitude(req.longitude());
        o.setDescricao(UsuarioBO.textoOpcional(req.descricao()));
        o.setUrlFoto(UsuarioBO.textoOpcional(req.urlFoto()));
        o.setNivelRisco(tipo.getPesoRisco());
    }

    /** Validacoes de seguranca (o controller ja aplica Bean Validation). */
    private void validar(OcorrenciaRequest req) {
        if (req == null) {
            throw new ValidacaoException("Informe os dados da ocorrência.");
        }
        if (req.enderecoReferencia() == null || req.enderecoReferencia().isBlank()) {
            throw new ValidacaoException("Informe o endereço de referência.");
        }
        if (req.enderecoReferencia().trim().length() > Ocorrencia.TAMANHO_MAXIMO_ENDERECO) {
            throw new ValidacaoException("O endereço de referência deve ter no máximo 150 caracteres.");
        }
        if (req.latitude() == null || req.longitude() == null) {
            throw new ValidacaoException("Informe a latitude e a longitude.");
        }
        if (req.latitude() < -90 || req.latitude() > 90 || req.longitude() < -180 || req.longitude() > 180) {
            throw new ValidacaoException("Coordenadas fora da faixa válida.");
        }
        if (req.descricao() != null && req.descricao().trim().length() > Ocorrencia.TAMANHO_MAXIMO_DESCRICAO) {
            throw new ValidacaoException("A descrição deve ter no máximo 500 caracteres.");
        }
    }

    private TipoOcorrencia exigirTipo(Long id) {
        if (id == null) {
            throw new ValidacaoException("Informe o tipo de ocorrência.");
        }
        return tipoOcorrenciaDAO.buscarPorId(id)
                .orElseThrow(() -> new RegraDeNegocioException("Tipo de ocorrência informado não existe."));
    }

    private Bairro exigirBairro(Long id) {
        if (id == null) {
            throw new ValidacaoException("Informe o bairro.");
        }
        return bairroDAO.buscarPorId(id)
                .orElseThrow(() -> new RegraDeNegocioException("Bairro informado não existe."));
    }
}
