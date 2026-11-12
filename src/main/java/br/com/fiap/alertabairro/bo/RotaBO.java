package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaRequest;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaResponse;
import br.com.fiap.alertabairro.dto.RotaAvaliadaResponse;
import br.com.fiap.alertabairro.dto.RotaCandidataRequest;
import br.com.fiap.alertabairro.dto.ZonaRiscoResponse;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.Ocorrencia;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras de rota segura.
 *
 * Zonas de risco: ocorrencias ALAGADO nao resolvidas e com localizacao,
 * cada uma com raio de 40 m. Uma rota atravessa uma zona quando a distancia
 * da zona ate a polilinha da rota e <= 40 m. A rota mais segura e a que
 * atravessa menos zonas e, no empate, a de menor duracao.
 *
 * Conversao para metros (projecao local): 110540 m por grau de latitude e
 * 111320 * cos(lat) m por grau de longitude, com distancia de ponto a segmento.
 */
@Service
public class RotaBO {

    public static final double RAIO_ZONA_METROS = 40.0;
    public static final double METROS_POR_GRAU_LATITUDE = 110_540.0;
    public static final double METROS_POR_GRAU_LONGITUDE_EQUADOR = 111_320.0;

    private final OcorrenciaDAO ocorrenciaDAO;
    private final UsuarioBO usuarioBO;

    public RotaBO(OcorrenciaDAO ocorrenciaDAO, UsuarioBO usuarioBO) {
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.usuarioBO = usuarioBO;
    }

    // ------------------------------------------------------------------
    // Zonas de risco
    // ------------------------------------------------------------------

    public List<ZonaRiscoResponse> listarZonasDeRisco(Long usuarioId) {
        usuarioBO.autenticar(usuarioId);
        return buscarZonas().stream()
                .map(o -> ZonaRiscoResponse.de(o, RAIO_ZONA_METROS))
                .toList();
    }

    /** Pontos alagados ativos (nao resolvidos) que possuem localizacao. */
    List<Ocorrencia> buscarZonas() {
        return ocorrenciaDAO.listarAlagamentosAtivos().stream()
                .filter(o -> o.isAlagamento() && !o.isResolvida() && o.possuiLocalizacao())
                .toList();
    }

    // ------------------------------------------------------------------
    // Avaliacao de rotas
    // ------------------------------------------------------------------

    public AvaliacaoRotaResponse avaliar(AvaliacaoRotaRequest request, Long usuarioId) {
        usuarioBO.autenticar(usuarioId);
        if (request == null || request.rotas() == null || request.rotas().isEmpty()) {
            throw new ValidacaoException("Informe ao menos uma rota candidata.");
        }
        List<Ocorrencia> zonas = buscarZonas();

        List<RotaAvaliadaResponse> parciais = new ArrayList<>();
        for (int i = 0; i < request.rotas().size(); i++) {
            RotaCandidataRequest rota = request.rotas().get(i);
            List<double[]> pontos = converterCoordenadas(rota, i);
            List<Long> atravessadas = zonas.stream()
                    .filter(z -> distanciaAtePolilinha(z.getLatitude(), z.getLongitude(), pontos) <= RAIO_ZONA_METROS)
                    .map(Ocorrencia::getId)
                    .toList();
            parciais.add(new RotaAvaliadaResponse(i, nomeDaRota(rota, i), atravessadas.size(), atravessadas,
                    rota.duracaoSegundos(), rota.distanciaMetros(), false));
        }

        int indiceMaisSegura = escolherMaisSegura(parciais);
        List<RotaAvaliadaResponse> rotas = parciais.stream()
                .map(r -> new RotaAvaliadaResponse(r.indice(), r.nome(), r.zonasAtravessadas(),
                        r.ocorrenciasNoCaminho(), r.duracaoSegundos(), r.distanciaMetros(),
                        r.indice() == indiceMaisSegura))
                .toList();
        return new AvaliacaoRotaResponse(zonas.size(), RAIO_ZONA_METROS, indiceMaisSegura, rotas);
    }

    /** Menos zonas atravessadas; no empate, menor duracao; persistindo o empate, a primeira enviada. */
    int escolherMaisSegura(List<RotaAvaliadaResponse> rotas) {
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

    private double duracao(RotaAvaliadaResponse rota) {
        return rota.duracaoSegundos() == null ? Double.MAX_VALUE : rota.duracaoSegundos();
    }

    private String nomeDaRota(RotaCandidataRequest rota, int indice) {
        return rota.nome() == null || rota.nome().isBlank() ? "Rota " + (indice + 1) : rota.nome().trim();
    }

    private List<double[]> converterCoordenadas(RotaCandidataRequest rota, int indice) {
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
