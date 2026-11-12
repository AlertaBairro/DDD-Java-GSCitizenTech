package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.model.AvaliacaoRisco;
import br.com.fiap.alertabairro.model.NivelRisco;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.fiap.alertabairro.bo.Dados.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("RiscoBO - cálculo de risco e priorização")
class RiscoBOTest {

    private final RiscoBO riscoBO = new RiscoBO(RELOGIO);

    @Test
    @DisplayName("ocorrência resolvida tem risco 0, nível BAIXO e rótulo Resolvido")
    void resolvidaTemRiscoZero() {
        Ocorrencia o = ocorrencia(1, SANTANA, ALAGADO, morador(1), 500, 30, StatusOcorrencia.RESOLVIDA);

        AvaliacaoRisco risco = riscoBO.calcular(o);

        assertThat(risco.getPontuacao()).isZero();
        assertThat(risco.getNivel()).isEqualTo(NivelRisco.BAIXO);
        assertThat(risco.getRotulo()).isEqualTo("Resolvido");
    }

    @Test
    @DisplayName("pontuação = peso * 2 + apoios + dias * 0.8")
    void aplicaFormula() {
        // BUEIRO (peso 4) ha 48 h (2 dias) com 3 apoios: 8 + 3 + 1.6 = 12.6
        Ocorrencia o = ocorrencia(1, SANTANA, BUEIRO, morador(1), 48, 3, StatusOcorrencia.RECEBIDA);

        AvaliacaoRisco risco = riscoBO.calcular(o);

        assertThat(risco.getPontuacao()).isCloseTo(12.6, within(0.0001));
        assertThat(risco.getNivel()).isEqualTo(NivelRisco.MEDIO);
        assertThat(risco.getRotulo()).isEqualTo("Risco médio");
    }

    @Test
    @DisplayName("ocorrência 1 do protótipo (BUEIRO, 52 h, 9 apoios) é de risco ALTO")
    void exemploDoPrototipo() {
        Ocorrencia o = ocorrencia(1, JARDIM_SAO_PAULO, BUEIRO, morador(1), 52, 9, StatusOcorrencia.RECEBIDA);

        AvaliacaoRisco risco = riscoBO.calcular(o);

        // 8 + 9 + (52/24)*0.8 = 18.733...
        assertThat(risco.getPontuacao()).isCloseTo(18.7333, within(0.001));
        assertThat(risco.getNivel()).isEqualTo(NivelRisco.ALTO);
    }

    @Test
    @DisplayName("limites dos níveis: >= 16 ALTO, >= 10 MÉDIO, abaixo BAIXO")
    void classificaNiveis() {
        assertThat(riscoBO.classificar(16.0)).isEqualTo(NivelRisco.ALTO);
        assertThat(riscoBO.classificar(15.99)).isEqualTo(NivelRisco.MEDIO);
        assertThat(riscoBO.classificar(10.0)).isEqualTo(NivelRisco.MEDIO);
        assertThat(riscoBO.classificar(9.99)).isEqualTo(NivelRisco.BAIXO);
        assertThat(riscoBO.classificar(0)).isEqualTo(NivelRisco.BAIXO);
    }

    @Test
    @DisplayName("priorização ordena por pontuação decrescente (resolvida vai para o fim)")
    void priorizaPorPontuacao() {
        // ALAGADO, 2 h, 6 apoios => 10 + 6 + 0.07 = 16.07 (ALTO)
        Ocorrencia alta = ocorrencia(1, SANTANA, ALAGADO, morador(1), 2, 6, StatusOcorrencia.RECEBIDA);
        // BOCA, 76 h, 4 apoios => 6 + 4 + 2.53 = 12.53 (MEDIO)
        Ocorrencia media = ocorrencia(2, SANTANA, BOCA, morador(1), 76, 4, StatusOcorrencia.RECEBIDA);
        // LIXO, 30 h, 3 apoios => 4 + 3 + 1.0 = 8.0 (BAIXO)
        Ocorrencia baixa = ocorrencia(3, TUCURUVI, LIXO, morador(1), 30, 3, StatusOcorrencia.EM_ATENDIMENTO);
        // resolvida => 0
        Ocorrencia resolvida = ocorrencia(4, SANTANA, ALAGADO, morador(1), 2, 50, StatusOcorrencia.RESOLVIDA);

        List<Long> ordem = riscoBO.priorizar(List.of(resolvida, baixa, alta, media)).stream()
                .map(p -> p.ocorrencia().getId())
                .toList();

        assertThat(ordem).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("com pontuações idênticas, a mais recente vem primeiro")
    void desempatePelaMaisRecente() {
        // ambas com 0 dias => 4 + 6 = 10.0; a "recente" foi criada 1 segundo depois
        Ocorrencia antiga = ocorrencia(1, SANTANA, LIXO, morador(1), 0, 6, StatusOcorrencia.RECEBIDA);
        Ocorrencia recente = ocorrencia(2, TUCURUVI, LIXO, morador(1), 0, 6, StatusOcorrencia.RECEBIDA);
        antiga.setDataCriacao(AGORA);
        recente.setDataCriacao(AGORA.plusSeconds(1));

        List<Long> ordem = riscoBO.priorizar(List.of(antiga, recente)).stream()
                .map(p -> p.ocorrencia().getId())
                .toList();

        assertThat(ordem).containsExactly(2L, 1L);
    }
}
