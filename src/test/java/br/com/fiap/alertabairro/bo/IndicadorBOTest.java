package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dao.TipoProblemaDAO;
import br.com.fiap.alertabairro.dto.ContagemResponse;
import br.com.fiap.alertabairro.dto.IndicadoresResponse;
import br.com.fiap.alertabairro.dto.ReincidenciaResponse;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.Perfil;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IndicadorBO - indicadores do gestor e reincidência")
class IndicadorBOTest {

    @Mock private OcorrenciaDAO ocorrenciaDAO;
    @Mock private BairroDAO bairroDAO;
    @Mock private TipoProblemaDAO tipoProblemaDAO;
    @Mock private UsuarioBO usuarioBO;

    private IndicadorBO indicadorBO;
    private List<Ocorrencia> seed;

    @BeforeEach
    void configurar() {
        indicadorBO = new IndicadorBO(ocorrenciaDAO, bairroDAO, tipoProblemaDAO, usuarioBO);
        Usuario autor = morador(1);

        // mesmas 6 ocorrencias do prototipo
        Ocorrencia o5 = ocorrencia(5, SANTANA, BUEIRO, autor, 120, 7, StatusOcorrencia.RESOLVIDA);
        o5.setDataResolucao(AGORA.minusHours(70));
        Ocorrencia o6 = ocorrencia(6, JARDIM_SAO_PAULO, BUEIRO, autor, 200, 5, StatusOcorrencia.RESOLVIDA);
        o6.setDataResolucao(AGORA.minusHours(150));
        seed = List.of(
                ocorrencia(1, JARDIM_SAO_PAULO, BUEIRO, autor, 52, 9, StatusOcorrencia.RECEBIDA),
                ocorrencia(2, JARDIM_SAO_PAULO, ALAGADO, autor, 2, 6, StatusOcorrencia.RECEBIDA),
                ocorrencia(3, TUCURUVI, LIXO, autor, 30, 3, StatusOcorrencia.EM_ATENDIMENTO),
                ocorrencia(4, SANTANA, BOCA, autor, 76, 4, StatusOcorrencia.RECEBIDA),
                o5,
                o6);
    }

    @Test
    @DisplayName("totais, tempo médio de resolução e alertas com os dados do protótipo")
    void indicadoresDoPrototipo() {
        IndicadoresResponse r = indicadorBO.calcular(seed,
                List.of(SANTANA, JARDIM_SAO_PAULO, TUCURUVI), List.of(ALAGADO, BUEIRO, BOCA, LIXO));

        assertThat(r.totalOcorrencias()).isEqualTo(6);
        assertThat(r.totalEmAberto()).isEqualTo(4);
        assertThat(r.totalResolvidas()).isEqualTo(2);
        // (120-70) e (200-150) => media de 50 h
        assertThat(r.tempoMedioResolucaoHoras()).isEqualTo(50.0);
        assertThat(r.tempoMedioResolucaoTexto()).isEqualTo("2 d 2 h");
        assertThat(r.totalAlertasAlagamento()).isEqualTo(1);
        assertThat(r.alertasAlagamentoAtivos()).isEqualTo(1);

        assertThat(r.porBairro()).extracting(ContagemResponse::nome, ContagemResponse::total)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Jardim São Paulo", 3L),
                        org.assertj.core.groups.Tuple.tuple("Santana", 2L),
                        org.assertj.core.groups.Tuple.tuple("Tucuruvi", 1L));
        assertThat(r.porTipo()).extracting(ContagemResponse::codigo).first().isEqualTo("BUEIRO");
    }

    @Test
    @DisplayName("reincidência: combinação bairro + tipo com mais de 1 ocorrência")
    void reincidencia() {
        List<ReincidenciaResponse> reincidencias = indicadorBO.reincidencias(seed);

        assertThat(reincidencias).hasSize(1);
        assertThat(reincidencias.get(0).bairro()).isEqualTo("Jardim São Paulo");
        assertThat(reincidencias.get(0).tipo()).isEqualTo("BUEIRO");
        assertThat(reincidencias.get(0).total()).isEqualTo(2);
    }

    @Test
    @DisplayName("sem ocorrências resolvidas, o tempo médio fica nulo")
    void semResolvidas() {
        IndicadoresResponse r = indicadorBO.calcular(seed.subList(0, 4), List.of(), List.of());

        assertThat(r.tempoMedioResolucaoHoras()).isNull();
        assertThat(r.reincidencias()).isEmpty();
    }

    @Test
    @DisplayName("formata o tempo médio em dias, horas e minutos")
    void formataTempo() {
        assertThat(IndicadorBO.formatarHoras(50.0)).isEqualTo("2 d 2 h");
        assertThat(IndicadorBO.formatarHoras(5.5)).isEqualTo("5 h 30 min");
        assertThat(IndicadorBO.formatarHoras(0.25)).isEqualTo("15 min");
        assertThat(IndicadorBO.formatarHoras(48.0)).isEqualTo("2 d");
    }

    @Test
    @DisplayName("somente o gestor acessa os indicadores (403)")
    void somenteGestor() {
        when(usuarioBO.autenticarComPerfil(eq(1L), eq(Perfil.GESTOR), anyString()))
                .thenThrow(new AcessoNegadoException("Somente o perfil Gestor da subprefeitura pode ver os indicadores."));

        assertThatThrownBy(() -> indicadorBO.obterIndicadores(1L))
                .isInstanceOf(AcessoNegadoException.class);
    }
}
