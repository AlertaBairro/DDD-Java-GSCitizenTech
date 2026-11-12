package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ConsultaRotaDAO;
import br.com.fiap.alertabairro.dao.PontoAlagadoDAO;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaRequest;
import br.com.fiap.alertabairro.dto.AvaliacaoRotaResponse;
import br.com.fiap.alertabairro.dto.LocalRotaRequest;
import br.com.fiap.alertabairro.dto.RotaCandidataRequest;
import br.com.fiap.alertabairro.dto.ZonaRiscoResponse;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.ConsultaRota;
import br.com.fiap.alertabairro.model.NivelAgua;
import br.com.fiap.alertabairro.model.PontoAlagado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RotaBOTest {

    /** Ponto 6 do DML do grupo: em frente a Estacao Jardim Sao Paulo (ativo). */
    private static final double LAT = -23.4922;
    private static final double LNG = -46.6166;

    private static final LocalRotaRequest ORIGEM = new LocalRotaRequest("Rua Dr. Zuquim, 900", -23.4935, -46.6180);
    private static final LocalRotaRequest DESTINO = new LocalRotaRequest("Estação Jardim São Paulo-Ayrton Senna",
            -23.4910, -46.6160);

    private PontoAlagadoDAO pontoDAO;
    private ConsultaRotaDAO consultaDAO;
    private RotaBO bo;

    @BeforeEach
    void preparar() {
        pontoDAO = mock(PontoAlagadoDAO.class);
        consultaDAO = mock(ConsultaRotaDAO.class);
        var usuarioDAO = Dados.usuarioDAO();
        bo = new RotaBO(pontoDAO, consultaDAO, Dados.usuarioBO(usuarioDAO, Dados.bairroDAO()), Dados.RELOGIO);

        PontoAlagado ponto = new PontoAlagado();
        ponto.setId(6L);
        ponto.setLatitude(LAT);
        ponto.setLongitude(LNG);
        ponto.setNivelAgua(NivelAgua.TORNOZELO);
        ponto.setAtivo(true);
        when(pontoDAO.listar(true)).thenReturn(List.of(ponto));
        when(consultaDAO.inserir(any())).thenAnswer(inv -> {
            ConsultaRota c = inv.getArgument(0);
            c.setId(50L);
            return c;
        });
    }

    /** Rota vertical passando exatamente pelo ponto alagado. */
    private RotaCandidataRequest pelaEstacao(double duracao) {
        return new RotaCandidataRequest("Pela estação",
                List.of(List.of(LAT - 0.001, LNG), List.of(LAT + 0.001, LNG)), duracao, 220.0);
    }

    /** Rota paralela ~200 m a leste: nao atravessa a zona. */
    private RotaCandidataRequest desvio(double duracao) {
        return new RotaCandidataRequest("Desvio",
                List.of(List.of(LAT - 0.001, LNG + 0.002), List.of(LAT + 0.001, LNG + 0.002)), duracao, 300.0);
    }

    private ConsultaRota consultaGravada() {
        ArgumentCaptor<ConsultaRota> captor = ArgumentCaptor.forClass(ConsultaRota.class);
        verify(consultaDAO).inserir(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Zonas de risco: pontos alagados ativos com raio de 40 m")
    void zonasDeRisco() {
        List<ZonaRiscoResponse> zonas = bo.listarZonasDeRisco(Dados.MORADOR);
        assertThat(zonas).singleElement().satisfies(z -> {
            assertThat(z.pontoAlagadoId()).isEqualTo(6L);
            assertThat(z.raioMetros()).isEqualTo(40);
        });
    }

    @Test
    @DisplayName("Rota padrão atravessa a zona e o desvio não: segura é o desvio e 1 ponto evitado, gravado na consulta")
    void rotaSeguraEvitaPonto() {
        AvaliacaoRotaResponse r = bo.avaliar(new AvaliacaoRotaRequest(ORIGEM, DESTINO,
                List.of(pelaEstacao(120), desvio(180))), Dados.MORADOR);

        assertThat(r.indiceMaisSegura()).isEqualTo(1);
        assertThat(r.rotas().get(0).zonasAtravessadas()).isEqualTo(1);
        assertThat(r.rotas().get(0).pontosAlagadosNoCaminho()).containsExactly(6L);
        assertThat(r.rotas().get(1).zonasAtravessadas()).isZero();
        assertThat(r.rotas().get(1).maisSegura()).isTrue();
        assertThat(r.pontosEvitados()).isEqualTo(1);
        assertThat(r.consultaId()).isEqualTo(50L);

        ConsultaRota c = consultaGravada();
        assertThat(c.getIdUsuario()).isEqualTo(Dados.MORADOR);
        assertThat(c.isRotaCalculada()).isTrue();
        assertThat(c.getPontosEvitados()).isEqualTo(1);
        assertThat(c.getOrigem()).isEqualTo("Rua Dr. Zuquim, 900");
        assertThat(c.getDestino()).startsWith("Estação Jardim São Paulo");
        assertThat(c.getDataConsulta()).isEqualTo(Dados.AGORA);
    }

    @Test
    @DisplayName("Se a rota padrão já é a mais segura, pontos evitados = 0")
    void padraoJaSegura() {
        AvaliacaoRotaResponse r = bo.avaliar(new AvaliacaoRotaRequest(ORIGEM, DESTINO,
                List.of(desvio(180), pelaEstacao(120))), Dados.MORADOR);
        assertThat(r.indiceMaisSegura()).isZero();
        assertThat(r.pontosEvitados()).isZero();
        assertThat(consultaGravada().getPontosEvitados()).isZero();
    }

    @Test
    @DisplayName("Empate de zonas: vence a de menor duração")
    void empatePorDuracao() {
        AvaliacaoRotaResponse r = bo.avaliar(new AvaliacaoRotaRequest(ORIGEM, DESTINO,
                List.of(desvio(300), desvio(200))), Dados.MORADOR);
        assertThat(r.indiceMaisSegura()).isEqualTo(1);
        assertThat(r.pontosEvitados()).isZero();
    }

    @Test
    @DisplayName("Sem rotas candidatas: consulta gravada com rota não calculada e 0 pontos evitados")
    void semRotas() {
        AvaliacaoRotaResponse r = bo.avaliar(new AvaliacaoRotaRequest(ORIGEM, DESTINO, List.of()), Dados.MORADOR);
        assertThat(r.rotaCalculada()).isFalse();
        assertThat(r.indiceMaisSegura()).isNull();
        ConsultaRota c = consultaGravada();
        assertThat(c.isRotaCalculada()).isFalse();
        assertThat(c.getPontosEvitados()).isZero();
    }

    @Test
    @DisplayName("Rota com menos de 2 coordenadas dá 400 e nada é gravado")
    void rotaInvalida() {
        RotaCandidataRequest invalida = new RotaCandidataRequest("x", List.of(List.of(LAT, LNG)), 10.0, 10.0);
        assertThatThrownBy(() -> bo.avaliar(new AvaliacaoRotaRequest(ORIGEM, DESTINO, List.of(invalida)), Dados.MORADOR))
                .isInstanceOf(ValidacaoException.class);
        verify(consultaDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Origem ou destino ausentes dão 400")
    void origemObrigatoria() {
        assertThatThrownBy(() -> bo.avaliar(new AvaliacaoRotaRequest(null, DESTINO, List.of()), Dados.MORADOR))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("Geometria: distância ponto-segmento com 110540 m/grau de latitude")
    void geometria() {
        double[] a = {LAT - 0.001, LNG + 0.0004};
        double[] b = {LAT + 0.001, LNG + 0.0004};
        double esperado = 0.0004 * 111_320 * Math.cos(Math.toRadians(LAT));
        assertThat(RotaBO.distanciaAteSegmento(LAT, LNG, a, b)).isCloseTo(esperado, within(0.01));
        // alem da ponta do segmento: distancia ate a extremidade
        double[] c = {LAT + 0.001, LNG};
        double[] d = {LAT + 0.002, LNG};
        assertThat(RotaBO.distanciaAteSegmento(LAT, LNG, c, d)).isCloseTo(110.54, within(0.01));
    }
}
