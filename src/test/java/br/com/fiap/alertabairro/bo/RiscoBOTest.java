package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.bo.RiscoBO.OcorrenciaPriorizada;
import br.com.fiap.alertabairro.model.AvaliacaoRisco;
import br.com.fiap.alertabairro.model.NivelRisco;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RiscoBOTest {

    private final RiscoBO bo = new RiscoBO(Dados.RELOGIO);

    @Test
    @DisplayName("Pontuação = nivelRisco * 2 + apoios + dias * 0.8")
    void formula() {
        Ocorrencia o = Dados.ocorrencia(1L, Dados.MORADOR, StatusOcorrencia.ABERTA, 4, 3, Dados.AGORA.minusDays(5));
        AvaliacaoRisco r = bo.calcular(o);
        assertThat(r.getPontuacao()).isCloseTo(4 * 2 + 3 + 5 * 0.8, within(0.0001));
        assertThat(r.getNivel()).isEqualTo(NivelRisco.MEDIO);
    }

    @Test
    @DisplayName("Limites: >= 16 ALTO, >= 10 MÉDIO, abaixo BAIXO")
    void limites() {
        assertThat(bo.classificar(16.0)).isEqualTo(NivelRisco.ALTO);
        assertThat(bo.classificar(15.99)).isEqualTo(NivelRisco.MEDIO);
        assertThat(bo.classificar(10.0)).isEqualTo(NivelRisco.MEDIO);
        assertThat(bo.classificar(9.99)).isEqualTo(NivelRisco.BAIXO);
    }

    @Test
    @DisplayName("Resolvida tem pontuação 0 e rótulo Resolvido")
    void resolvida() {
        Ocorrencia o = Dados.ocorrencia(1L, Dados.MORADOR, StatusOcorrencia.RESOLVIDA, 5, 10, Dados.AGORA.minusDays(9));
        AvaliacaoRisco r = bo.calcular(o);
        assertThat(r.getPontuacao()).isZero();
        assertThat(r.getRotulo()).isEqualTo(AvaliacaoRisco.ROTULO_RESOLVIDO);
    }

    @Test
    @DisplayName("Registro no futuro não gera dias negativos")
    void semDiasNegativos() {
        Ocorrencia o = Dados.ocorrencia(1L, Dados.MORADOR, StatusOcorrencia.ABERTA, 1, 0, Dados.AGORA.plusHours(3));
        assertThat(bo.diasDesdeRegistro(o, Dados.AGORA)).isZero();
    }

    @Test
    @DisplayName("Priorização: pontuação decrescente")
    void ordenacao() {
        Ocorrencia media = Dados.ocorrencia(1L, Dados.MORADOR, StatusOcorrencia.ABERTA, 3, 4, Dados.AGORA.minusDays(1));
        Ocorrencia baixa = Dados.ocorrencia(2L, Dados.MORADOR, StatusOcorrencia.ABERTA, 1, 0, Dados.AGORA);
        Ocorrencia alta = Dados.ocorrencia(3L, Dados.MORADOR, StatusOcorrencia.ABERTA, 5, 4, Dados.AGORA.minusDays(3));

        List<OcorrenciaPriorizada> fila = bo.priorizar(List.of(media, baixa, alta));

        assertThat(fila).extracting(p -> p.ocorrencia().getId()).containsExactly(3L, 1L, 2L);
        assertThat(fila.get(0).risco().getNivel()).isEqualTo(NivelRisco.ALTO);
        assertThat(fila.get(2).risco().getNivel()).isEqualTo(NivelRisco.BAIXO);
    }

    @Test
    @DisplayName("Priorização: empate exato desempata pela mais recente")
    void empateExato() {
        Ocorrencia a = Dados.ocorrencia(1L, Dados.MORADOR, StatusOcorrencia.RESOLVIDA, 3, 0, Dados.AGORA.minusDays(4));
        Ocorrencia b = Dados.ocorrencia(2L, Dados.MORADOR, StatusOcorrencia.RESOLVIDA, 3, 0, Dados.AGORA.minusDays(2));
        assertThat(bo.priorizar(List.of(a, b))).extracting(p -> p.ocorrencia().getId()).containsExactly(2L, 1L);
    }
}
