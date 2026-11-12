package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.PontoAlagadoDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.PontoAlagadoRequest;
import br.com.fiap.alertabairro.dto.PontoAlagadoResponse;
import br.com.fiap.alertabairro.dto.ReportePontoAlagadoResponse;
import br.com.fiap.alertabairro.dto.ResumoResponse;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.Alerta;
import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.NivelAgua;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.PontoAlagado;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pontos alagados:
 * <ul>
 *   <li>reporte pelo MORADOR: na mesma transacao cria o ponto ativo, um alerta
 *       (mensagem gerada do endereco e do nivel da agua, raio pelo nivel) e um
 *       destinatario para cada usuario ativo do mesmo bairro, exceto quem reportou;</li>
 *   <li>encerramento pelo AGENTE ou GESTOR (st_ativo = 'N' e data de encerramento).</li>
 * </ul>
 */
@Service
public class PontoAlagadoBO {

    private final PontoAlagadoDAO pontoAlagadoDAO;
    private final UsuarioDAO usuarioDAO;
    private final BairroDAO bairroDAO;
    private final UsuarioBO usuarioBO;
    private final Clock clock;

    public PontoAlagadoBO(PontoAlagadoDAO pontoAlagadoDAO, UsuarioDAO usuarioDAO, BairroDAO bairroDAO,
                          UsuarioBO usuarioBO, Clock clock) {
        this.pontoAlagadoDAO = pontoAlagadoDAO;
        this.usuarioDAO = usuarioDAO;
        this.bairroDAO = bairroDAO;
        this.usuarioBO = usuarioBO;
        this.clock = clock;
    }

    public List<PontoAlagadoResponse> listar(Long idUsuario, boolean somenteAtivos) {
        usuarioBO.autenticar(idUsuario);
        return montarRespostas(pontoAlagadoDAO.listar(somenteAtivos));
    }

    public PontoAlagadoResponse buscar(Long id, Long idUsuario) {
        usuarioBO.autenticar(idUsuario);
        return montarRespostas(List.of(buscarEntidade(id))).get(0);
    }

    public ReportePontoAlagadoResponse reportar(PontoAlagadoRequest req, Long idUsuario) {
        Usuario morador = usuarioBO.autenticarComPerfil(idUsuario, Perfil.MORADOR);
        validar(req);
        Bairro bairro = bairroDAO.buscarPorId(req.bairroId())
                .orElseThrow(() -> new RegraDeNegocioException("Bairro informado não existe."));
        LocalDateTime agora = LocalDateTime.now(clock);

        PontoAlagado ponto = new PontoAlagado();
        ponto.setIdUsuario(morador.getId());
        ponto.setIdBairro(bairro.getId());
        ponto.setEnderecoReferencia(req.enderecoReferencia().trim());
        ponto.setLatitude(req.latitude());
        ponto.setLongitude(req.longitude());
        ponto.setNivelAgua(req.nivelAgua());
        ponto.setDataReporte(agora);
        ponto.setAtivo(true);
        ponto.setDataEncerramento(null);

        Alerta alerta = new Alerta(null, null, gerarMensagem(ponto.getEnderecoReferencia(), req.nivelAgua()),
                req.nivelAgua().getRaioAlertaMetros(), agora);

        List<Long> destinatarios = usuarioDAO.listarIdsAtivosPorBairro(bairro.getId()).stream()
                .filter(id -> !id.equals(morador.getId()))
                .toList();

        int notificados = pontoAlagadoDAO.reportar(ponto, alerta, destinatarios);
        PontoAlagadoResponse resposta = montarRespostas(List.of(ponto)).get(0);
        return new ReportePontoAlagadoResponse(resposta, AlertaBO.montar(alerta, ponto), notificados);
    }

    public PontoAlagadoResponse encerrar(Long id, Long idUsuario) {
        usuarioBO.autenticarComPerfil(idUsuario, Perfil.AGENTE, Perfil.GESTOR);
        PontoAlagado ponto = buscarEntidade(id);
        if (!ponto.isAtivo()) {
            throw new RegraDeNegocioException("Este ponto alagado já foi encerrado.");
        }
        ponto.encerrar(LocalDateTime.now(clock));
        if (!pontoAlagadoDAO.encerrar(ponto)) {
            throw new RegraDeNegocioException("Este ponto alagado já foi encerrado.");
        }
        return montarRespostas(List.of(ponto)).get(0);
    }

    public PontoAlagado buscarEntidade(Long id) {
        return pontoAlagadoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ponto alagado não encontrado."));
    }

    /**
     * Mensagem do alerta gerada a partir do endereco e do nivel da agua
     * (limitada aos 300 caracteres de T_AB_ALERTA.ds_mensagem).
     */
    public static String gerarMensagem(String endereco, NivelAgua nivel) {
        String mensagem = nivel == NivelAgua.INTRANSITAVEL
                ? "Trecho intransitável por alagamento em " + endereco + ". Procure rota alternativa."
                : "Alagamento com água " + nivel.getDescricao() + " em " + endereco + ". Evite o trecho.";
        return mensagem.length() <= Alerta.TAMANHO_MAXIMO_MENSAGEM
                ? mensagem
                : mensagem.substring(0, Alerta.TAMANHO_MAXIMO_MENSAGEM - 3) + "...";
    }

    private List<PontoAlagadoResponse> montarRespostas(List<PontoAlagado> pontos) {
        if (pontos.isEmpty()) {
            return List.of();
        }
        Map<Long, String> bairros = bairroDAO.listar().stream()
                .collect(Collectors.toMap(Bairro::getId, Bairro::getNome));
        Set<Long> idsUsuarios = new HashSet<>();
        pontos.forEach(p -> idsUsuarios.add(p.getIdUsuario()));
        Map<Long, String> nomes = usuarioDAO.buscarNomes(idsUsuarios);

        return pontos.stream()
                .map(p -> new PontoAlagadoResponse(
                        p.getId(),
                        new ResumoResponse(p.getIdBairro(), bairros.get(p.getIdBairro())),
                        new ResumoResponse(p.getIdUsuario(), nomes.get(p.getIdUsuario())),
                        p.getEnderecoReferencia(),
                        p.getLatitude(),
                        p.getLongitude(),
                        p.getNivelAgua(),
                        p.getNivelAgua().getDescricao(),
                        p.getDataReporte(),
                        p.isAtivo(),
                        p.getDataEncerramento()))
                .toList();
    }

    private void validar(PontoAlagadoRequest req) {
        if (req == null || req.bairroId() == null || req.nivelAgua() == null
                || req.latitude() == null || req.longitude() == null) {
            throw new ValidacaoException("Informe bairro, endereço de referência, latitude, longitude e nível da água.");
        }
        if (req.enderecoReferencia() == null || req.enderecoReferencia().isBlank()) {
            throw new ValidacaoException("Informe o endereço de referência.");
        }
        if (req.enderecoReferencia().trim().length() > 150) {
            throw new ValidacaoException("O endereço de referência deve ter no máximo 150 caracteres.");
        }
        if (req.latitude() < -90 || req.latitude() > 90 || req.longitude() < -180 || req.longitude() > 180) {
            throw new ValidacaoException("Coordenadas fora da faixa válida.");
        }
    }
}
