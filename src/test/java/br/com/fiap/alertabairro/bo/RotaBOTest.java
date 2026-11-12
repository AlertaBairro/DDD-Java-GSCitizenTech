package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaRequest;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaResponse;
import br.com.fiap.alertabairro.dto.RotaCandidataRequest;
import br.com.fiap.alertabairro.dto.ZonaRiscoResponse;
import br.com.fiap.alertabairro.exception.ValidacaoException;
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

import java.util.List;

import static br.com.fiap.alertabairro.bo.Dados.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RotaBO - zonas de risco e rota mais segura")
class RotaBOTest {

    /** Ponto alagado do protótipo (ocorrência 2). */
    private static final double ZONA_LAT = -23.492702536565853;
    private static final double ZONA_LNG = -46.61682533666883;

    @Mock private OcorrenciaDAO ocorrenciaDAO;
    @Mock private UsuarioBO usuarioBO;

    private RotaBO rotaBO;
    private final Usuario morador = morador(1);

    @BeforeEach
    void configurar() {
        rotaBO = new RotaBO(ocorrenciaDAO, usuarioBO);
        when(usuarioBO.autenticar(1L)).thenReturn(morador);

        Ocorrencia alagado = comLocalizacao(
                ocorrencia(2, JARDIM_SAO_PAULO, ALAGADO, morador, 2, 6, StatusOcorrencia.RECEBIDA), ZONA_LAT, ZONA_LNG);
        Ocorrencia semLocalizacao = ocorrencia(3, SANTANA, ALAGADO, morador, 2, 1, StatusOcorrencia.RECEBIDA);
        when(ocorrenciaDAO.listarAlagamentosAtivos()).thenReturn(List.of(alagado, semLocalizacao));
    }

    @Test
    @DisplayName("distância de ponto a segmento usa 110540 m/grau de lat e 111320*cos(lat) m/grau de lng")
    void distanciaPontoSegmento() {
        // segmento horizontal 0.001 grau ao norte do ponto: ~110.54 m
        double d = RotaBO.distanciaAteSegmento(0, 0, new double[]{0.001, -0.01}, new double[]{0.001, 0.01});
        assertThat(d).isCloseTo(110.54, within(0.01));

        // ponto exatamente sobre o segmento
        double sobre = RotaBO.distanciaAteSegmento(-23.5, -46.6,
                new double[]{-23.5, -46.61}, new double[]{-23.5, -46.59});
        assertThat(sobre).isCloseTo(0.0, within(0.0001));
    }

    @Test
    @DisplayName("distância usa a extremidade quando a projeção cai fora do segmento")
    void distanciaAteExtremidade() {
        // segmento a leste do ponto, comecando 0.001 grau de longitude no equador (~111.32 m)
        double d = RotaBO.distanciaAteSegmento(0, 0, new double[]{0, 0.001}, new double[]{0, 0.002});
        assertThat(d).isCloseTo(111.32, within(0.01));
    }

    @Test
    @DisplayName("zonas de risco: só ALAGADO ativo com localização, raio de 40 m")
    void zonasDeRisco() {
        List<ZonaRiscoResponse> zonas = rotaBO.listarZonasDeRisco(1L);

        assertThat(zonas).hasSize(1);
        assertThat(zonas.get(0).ocorrenciaId()).isEqualTo(2L);
        assertThat(zonas.get(0).raioMetros()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("rota que passa a menos de 40 m da zona a atravessa; a outra é a mais segura")
    void escolheRotaQueDesviaDaZona() {
        // rota A passa exatamente pelo ponto alagado (mais rapida)
        RotaCandidataRequest passaNaZona = new RotaCandidataRequest("A", List.of(
                List.of(ZONA_LAT - 0.001, ZONA_LNG), List.of(ZONA_LAT + 0.001, ZONA_LNG)), 100.0, 220.0);
        // rota B passa ~200 m a leste (mais lenta)
        double lngDesvio = ZONA_LNG + 0.002;
        RotaCandidataRequest desvia = new RotaCandidataRequest("B", List.of(
                List.of(ZONA_LAT - 0.001, lngDesvio), List.of(ZONA_LAT + 0.001, lngDesvio)), 160.0, 300.0);

        AvaliacaoRotaResponse resposta = rotaBO.avaliar(new AvaliacaoRotaRequest(List.of(passaNaZona, desvia)), 1L);

        assertThat(resposta.totalZonasDeRisco()).isEqualTo(1);
        assertThat(resposta.rotas().get(0).zonasAtravessadas()).isEqualTo(1);
        assertThat(resposta.rotas().get(0).ocorrenciasNoCaminho()).containsExactly(2L);
        assertThat(resposta.rotas().get(1).zonasAtravessadas()).isZero();
        assertThat(resposta.indiceRotaMaisSegura()).isEqualTo(1);
        assertThat(resposta.rotas().get(1).maisSegura()).isTrue();
        assertThat(resposta.rotas().get(0).maisSegura()).isFalse();
    }

    @Test
    @DisplayName("empate em zonas atravessadas: vence a de menor duração")
    void desempatePelaDuracao() {
        double lngLonge1 = ZONA_LNG + 0.003;
        double lngLonge2 = ZONA_LNG - 0.003;
        RotaCandidataRequest lenta = new RotaCandidataRequest(null, List.of(
                List.of(ZONA_LAT - 0.001, lngLonge1), List.of(ZONA_LAT + 0.001, lngLonge1)), 300.0, 400.0);
        RotaCandidataRequest rapida = new RotaCandidataRequest(null, List.of(
                List.of(ZONA_LAT - 0.001, lngLonge2), List.of(ZONA_LAT + 0.001, lngLonge2)), 200.0, 400.0);

        AvaliacaoRotaResponse resposta = rotaBO.avaliar(new AvaliacaoRotaRequest(List.of(lenta, rapida)), 1L);

        assertThat(resposta.indiceRotaMaisSegura()).isEqualTo(1);
        assertThat(resposta.rotas().get(1).nome()).isEqualTo("Rota 2");
    }

    @Test
    @DisplayName("rota com menos de 2 coordenadas é rejeitada (400)")
    void rotaInvalida() {
        RotaCandidataRequest invalida = new RotaCandidataRequest(null, List.of(List.of(ZONA_LAT, ZONA_LNG)), 10.0, 10.0);

        assertThatThrownBy(() -> rotaBO.avaliar(new AvaliacaoRotaRequest(List.of(invalida)), 1L))
                .isInstanceOf(ValidacaoException.class);
    }
}
