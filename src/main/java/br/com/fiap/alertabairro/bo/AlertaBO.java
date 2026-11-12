package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.AlertaDAO;
import br.com.fiap.alertabairro.dao.AlertaUsuarioDAO;
import br.com.fiap.alertabairro.dao.PontoAlagadoDAO;
import br.com.fiap.alertabairro.dto.AlertaResponse;
import br.com.fiap.alertabairro.dto.MeuAlertaResponse;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.model.Alerta;
import br.com.fiap.alertabairro.model.AlertaUsuario;
import br.com.fiap.alertabairro.model.PontoAlagado;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Alertas de alagamento:
 * <ul>
 *   <li>alertas ativos: os de pontos alagados ainda ativos;</li>
 *   <li>alertas do usuario: os recebidos (lidos e nao lidos) por quem esta no header;</li>
 *   <li>marcar como lido: grava dt_leitura (uma unica vez).</li>
 * </ul>
 */
@Service
public class AlertaBO {

    private final AlertaDAO alertaDAO;
    private final AlertaUsuarioDAO alertaUsuarioDAO;
    private final PontoAlagadoDAO pontoAlagadoDAO;
    private final UsuarioBO usuarioBO;
    private final Clock clock;

    public AlertaBO(AlertaDAO alertaDAO, AlertaUsuarioDAO alertaUsuarioDAO, PontoAlagadoDAO pontoAlagadoDAO,
                    UsuarioBO usuarioBO, Clock clock) {
        this.alertaDAO = alertaDAO;
        this.alertaUsuarioDAO = alertaUsuarioDAO;
        this.pontoAlagadoDAO = pontoAlagadoDAO;
        this.usuarioBO = usuarioBO;
        this.clock = clock;
    }

    public List<AlertaResponse> listarAtivos(Long idUsuario) {
        usuarioBO.autenticar(idUsuario);
        List<Alerta> alertas = alertaDAO.listarAtivos();
        Map<Long, PontoAlagado> pontos = pontoAlagadoDAO.buscarPorIds(
                alertas.stream().map(Alerta::getIdPontoAlagado).collect(Collectors.toSet()));
        return alertas.stream().map(a -> montar(a, pontos.get(a.getIdPontoAlagado()))).toList();
    }

    public List<MeuAlertaResponse> listarMeus(Long idUsuario) {
        Usuario usuario = usuarioBO.autenticar(idUsuario);
        List<AlertaUsuario> recebidos = alertaUsuarioDAO.listarPorUsuario(usuario.getId());
        if (recebidos.isEmpty()) {
            return List.of();
        }
        Map<Long, Alerta> alertas = alertaDAO.buscarPorIds(
                recebidos.stream().map(AlertaUsuario::getIdAlerta).collect(Collectors.toSet()));
        Map<Long, PontoAlagado> pontos = pontoAlagadoDAO.buscarPorIds(
                alertas.values().stream().map(Alerta::getIdPontoAlagado).collect(Collectors.toSet()));
        return recebidos.stream()
                .filter(au -> alertas.containsKey(au.getIdAlerta()))
                .map(au -> {
                    Alerta alerta = alertas.get(au.getIdAlerta());
                    return montarMeu(au, alerta, pontos.get(alerta.getIdPontoAlagado()));
                })
                .toList();
    }

    /** Marca o alerta como lido pelo usuario do header (idempotente). */
    public MeuAlertaResponse marcarComoLido(Long idAlerta, Long idUsuario) {
        Usuario usuario = usuarioBO.autenticar(idUsuario);
        AlertaUsuario recebido = alertaUsuarioDAO.buscar(idAlerta, usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado entre os seus alertas."));
        if (!recebido.isLido()) {
            recebido.marcarComoLido(LocalDateTime.now(clock));
            alertaUsuarioDAO.registrarLeitura(recebido);
        }
        Alerta alerta = alertaDAO.buscarPorId(idAlerta)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado."));
        PontoAlagado ponto = pontoAlagadoDAO.buscarPorId(alerta.getIdPontoAlagado()).orElse(null);
        return montarMeu(recebido, alerta, ponto);
    }

    static AlertaResponse montar(Alerta a, PontoAlagado p) {
        return new AlertaResponse(
                a.getId(),
                a.getIdPontoAlagado(),
                a.getMensagem(),
                a.getRaioMetros(),
                a.getDataEnvio(),
                p != null ? p.getEnderecoReferencia() : null,
                p != null ? p.getLatitude() : null,
                p != null ? p.getLongitude() : null,
                p != null ? p.getNivelAgua() : null,
                p != null && p.isAtivo());
    }

    private static MeuAlertaResponse montarMeu(AlertaUsuario au, Alerta a, PontoAlagado p) {
        return new MeuAlertaResponse(montar(a, p), au.getDataRecebimento(), au.getDataLeitura(), au.isLido());
    }
}
