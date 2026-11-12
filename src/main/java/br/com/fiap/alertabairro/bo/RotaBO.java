package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ConsultaRotaDAO;
import br.com.fiap.alertabairro.dao.PontoAlagadoDAO;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaRequest;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaResponse;
import br.com.fiap.alertabairro.dto.ConsultaRotaResponse;
import br.com.fiap.alertabairro.dto.LocalRotaRequest;
import br.com.fiap.alertabairro.dto.RotaAvaliadaResponse;
import br.com.fiap.alertabairro.dto.RotaCandidataRequest;
import br.com.fiap.alertabairro.dto.ZonaRiscoResponse;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.ConsultaRota;
import br.com.fiap.alertabairro.model.PontoAlagado;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rota segura.
 *
 * Zonas de risco: pontos alagados ATIVOS, cada um com raio de desvio de 40 m
 * (diferente do raio de divulgacao do alerta). Uma rota atravessa a zona quando
 * a distancia do ponto ate a polilinha e menor ou igual a 40 m. A rota mais
 * segura e a de menos zonas atravessadas; no empate, a de menor duracao.
 *
 * Cada avaliacao e gravada em T_AB_CONSULTA_ROTA: st_rota_calculada = 'S' se
 * houve rota e nr_pontos_evitados = zonas da rota padrao (a primeira) menos
 * zonas da rota segura (minimo 0).
 */
@Service
public class RotaBO {

    public static final int RAIO_ZONA_METROS = 40;
    public static final double METROS_POR_GRAU_LATITUDE = 110_540.0;
    public static final double METROS_POR_GRAU_LONGITUDE_EQUADOR = 111_320.0;

    private final PontoAlagadoDAO pontoAlagadoDAO;
    private final ConsultaRotaDAO consultaRotaDAO;
    private final UsuarioBO usuarioBO;
    private final Clock clock;

    public RotaBO(PontoAlagadoDAO pontoAlagadoDAO, ConsultaRotaDAO consultaRotaDAO, UsuarioBO usuarioBO,
                  Clock clock) {
        this.pontoAlagadoDAO = pontoAlagadoDAO;
        this.consultaRotaDAO = consultaRotaDAO;
        this.usuarioBO = usuarioBO;
        this.clock = clock;
    }

    // ------------------------------------------------------------------
    // Zonas de risco
    // ------------------------------------------------------------------

    public List<ZonaRiscoResponse> listarZonasDeRisco(Long idUsuario) {
        usuarioBO.autenticar(idUsuario);
        return pontoAlagadoDAO.listar(true).stream()
                .map(p -> new ZonaRiscoResponse(p.getId(), p.getEnderecoReferencia(), p.getLatitude(),
                        p.getLongitude(), p.getNivelAgua(), RAIO_ZONA_METROS, p.getDataReporte()))
                .toList();
    }

    // ------------------------------------------------------------------
    // Avaliacao de rotas
    // ------------------------------------------------------------------

    public AvaliacaoRotaResponse avaliar(AvaliacaoRotaRequest req, Long idUsuario) {
        Usuario usuario = usuarioBO.autenticar(idUsuario);
        if (req == null) {
            throw new ValidacaoException("Informe origem, destino e as rotas candidatas.");
        }
        validarLocal(req.origem(), "origem");
        validarLocal(req.destino(), "destino");
        List<RotaCandidataRequest> candidatas = req.rotas() == null ? List.of() : req.rotas();

        List<PontoAlagado> zonas = pontoAlagadoDAO.listar(true);
        List<RotaAvaliadaResponse> parciais = new ArrayList<>();
        for (int i = 0; i < candidatas.size(); i++) {
            RotaCandidataRequest rota = candidatas.get(i);
            List<double[]> pontos = converterCoordenadas(rota, i);
            List<Long> atravessadas = zonas.stream()
                    .filter(z -> distanciaAtePolilinha(z.getLatitude(), z.getLongitude(), pontos) <= RAIO_ZONA_METROS)
                    .map(PontoAlagado::getId)
                    .toList();
            parciais.add(new RotaAvaliadaResponse(i, nomeDaRota(rota, i), atravessadas.size(), atravessadas,
                    rota.duracaoSegundos(), rota.distanciaMetros(), false));
        }

        boolean rotaCalculada = !parciais.isEmpty();
        Integer indiceMaisSegura = rotaCalculada ? escolherMaisSegura(parciais) : null;
        int pontosEvitados = rotaCalculada
                ? calcularPontosEvitados(parciais.get(0), parciais.get(indiceMaisSegura))
                : 0;

        List<RotaAvaliadaResponse> rotas = parciais.stream()
                .map(r -> new RotaAvaliadaResponse(r.indice(), r.nome(), r.zonasAtravessadas(),
                        r.pontosAlagadosNoCaminho(), r.duracaoSegundos(), r.distanciaMetros(),
                        indiceMaisSegura != null && r.indice() == indiceMaisSegura))
                .toList();

        ConsultaRota consulta = new ConsultaRota();
        consulta.setIdUsuario(usuario.getId());
        consulta.setOrigem(limitar(req.origem().descricao()));
        consulta.setLatitudeOrigem(req.origem().latitude());
        consulta.setLongitudeOrigem(req.origem().longitude());
        consulta.setDestino(limitar(req.destino().descricao()));
        consulta.setLatitudeDestino(req.destino().latitude());
        consulta.setLongitudeDestino(req.destino().longitude());
        consulta.setDataConsulta(LocalDateTime.now(clock));
        consulta.setRotaCalculada(rotaCalculada);
        consulta.setPontosEvitados(pontosEvitados);
        consultaRotaDAO.inserir(consulta);

        return new AvaliacaoRotaResponse(consulta.getId(), zonas.size(), RAIO_ZONA_METROS, rotaCalculada,
                indiceMaisSegura, pontosEvitados, rotas);
    }

    public List<ConsultaRotaResponse> listarMinhas(Long idUsuario) {
        Usuario usuario = usuarioBO.autenticar(idUsuario);
        return consultaRotaDAO.listarPorUsuario(usuario.getId()).stream().map(ConsultaRotaResponse::de).toList();
    }

    /** Zonas da rota padrao menos zonas da rota segura, nunca negativo (CK_AB_ROTA_PONTOS). */
    static int calcularPontosEvitados(RotaAvaliadaResponse padrao, RotaAvaliadaResponse segura) {
        return Math.max(0, padrao.zonasAtravessadas() - segura.zonasAtravessadas());
    }

    /** Menos zonas atravessadas; no empate, menor duracao; persistindo o empate, a primeira enviada. */
    static int escolherMaisSegura(List<RotaAvaliadaResponse> rotas) {
        RotaAvaliadaResponse melhor = rotas.get(0);
        for (RotaAvaliadaResponse rota : rotas) {
            if (rota.zonasAtravessadas() < melhor.zonasAtravessadas()
                    || (rota.zonasAtravessadas() == melhor.zonasAtravessadas()
                    && duracao(rota) < duracao(melhor))) {
                melhor = rota;
            }
        }
        return melhor.indice();
    }

    private static double duracao(RotaAvaliadaResponse rota) {
        return rota.duracaoSegundos() == null ? Double.MAX_VALUE : rota.duracaoSegundos();
    }

    private static String nomeDaRota(RotaCandidataRequest rota, int indice) {
        return rota.nome() == null || rota.nome().isBlank() ? "Rota " + (indice + 1) : rota.nome().trim();
    }

    private static String limitar(String texto) {
        String t = texto.trim();
        return t.length() <= ConsultaRota.TAMANHO_MAXIMO_DESCRICAO ? t
                : t.substring(0, ConsultaRota.TAMANHO_MAXIMO_DESCRICAO);
    }

    private static void validarLocal(LocalRotaRequest local, String nome) {
        if (local == null || local.descricao() == null || local.descricao().isBlank()
                || local.latitude() == null || local.longitude() == null) {
            throw new ValidacaoException("Informe a descrição e as coordenadas do " + nome + ".");
        }
        if (local.latitude() < -90 || local.latitude() > 90 || local.longitude() < -180 || local.longitude() > 180) {
            throw new ValidacaoException("Coordenadas do " + nome + " fora da faixa válida.");
        }
    }

    private static List<double[]> converterCoordenadas(RotaCandidataRequest rota, int indice) {
        String identificacao = "Rota " + (indice + 1);
        if (rota == null || rota.coordenadas() == null || rota.coordenadas().size() < 2) {
            throw new ValidacaoException(identificacao + ": informe ao menos 2 coordenadas [lat, lng].");
        }
        List<double[]> pontos = new ArrayList<>();
        for (List<Double> coordenada : rota.coordenadas()) {
            if (coordenada == null || coordenada.size() != 2
                    || coordenada.get(0) == null || coordenada.get(1) == null) {
                throw new ValidacaoException(identificacao + ": cada coordenada deve ser [lat, lng].");
            }
            double lat = coordenada.get(0);
            double lng = coordenada.get(1);
            if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                throw new ValidacaoException(identificacao + ": coordenada fora da faixa válida [" + lat + ", " + lng + "].");
            }
            pontos.add(new double[]{lat, lng});
        }
        if (rota.duracaoSegundos() != null && rota.duracaoSegundos() < 0) {
            throw new ValidacaoException(identificacao + ": a duração não pode ser negativa.");
        }
        return pontos;
    }

    // ------------------------------------------------------------------
    // Geometria
    // ------------------------------------------------------------------

    /** Menor distancia (em metros) de um ponto ate a polilinha formada pelos pontos [lat, lng]. */
    public static double distanciaAtePolilinha(double lat, double lng, List<double[]> polilinha) {
        if (polilinha.size() == 1) {
            return distanciaAteSegmento(lat, lng, polilinha.get(0), polilinha.get(0));
        }
        double menor = Double.MAX_VALUE;
        for (int i = 0; i < polilinha.size() - 1; i++) {
            menor = Math.min(menor, distanciaAteSegmento(lat, lng, polilinha.get(i), polilinha.get(i + 1)));
        }
        return menor;
    }

    /**
     * Distancia de um ponto P ao segmento AB, em metros. Os pontos sao
     * projetados num plano local centrado em P: y = dLat * 110540 e
     * x = dLng * 111320 * cos(latP).
     */
    public static double distanciaAteSegmento(double lat, double lng, double[] a, double[] b) {
        double metrosPorGrauLng = METROS_POR_GRAU_LONGITUDE_EQUADOR * Math.cos(Math.toRadians(lat));

        double ax = (a[1] - lng) * metrosPorGrauLng;
        double ay = (a[0] - lat) * METROS_POR_GRAU_LATITUDE;
        double bx = (b[1] - lng) * metrosPorGrauLng;
        double by = (b[0] - lat) * METROS_POR_GRAU_LATITUDE;

        double dx = bx - ax;
        double dy = by - ay;
        double comprimento2 = dx * dx + dy * dy;
        double t = comprimento2 == 0 ? 0 : -(ax * dx + ay * dy) / comprimento2;
        t = Math.max(0, Math.min(1, t));

        double px = ax + t * dx;
        double py = ay + t * dy;
        return Math.sqrt(px * px + py * py);
    }
}
