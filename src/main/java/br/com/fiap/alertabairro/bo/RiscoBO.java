package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.model.AvaliacaoRisco;
import br.com.fiap.alertabairro.model.NivelRisco;
import br.com.fiap.alertabairro.model.Ocorrencia;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Regras do calculo de risco das ocorrencias.
 *
 * RESOLVIDA: risco 0, nivel BAIXO, rotulo "Resolvido".
 * Demais: pontuacao = peso_do_tipo * 2 + apoios + dias_desde_criacao * 0.8
 * Nivel: >= 16 ALTO | >= 10 MEDIO | abaixo disso BAIXO.
 */
@Service
public class RiscoBO {

    public static final double LIMITE_ALTO = 16.0;
    public static final double LIMITE_MEDIO = 10.0;
    public static final double FATOR_PESO = 2.0;
    public static final double FATOR_DIAS = 0.8;

    private static final double MILISSEGUNDOS_POR_DIA = 86_400_000.0;

    private final Clock clock;

    public RiscoBO(Clock clock) {
        this.clock = clock;
    }

    public AvaliacaoRisco calcular(Ocorrencia ocorrencia) {
        return calcular(ocorrencia, OffsetDateTime.now(clock));
    }

    public AvaliacaoRisco calcular(Ocorrencia ocorrencia, OffsetDateTime agora) {
        if (ocorrencia.isResolvida()) {
            return new AvaliacaoRisco(0.0, NivelRisco.BAIXO, AvaliacaoRisco.ROTULO_RESOLVIDO);
        }
        double pontuacao = ocorrencia.getTipo().getPeso() * FATOR_PESO
                + ocorrencia.getQuantidadeApoios()
                + diasDesdeCriacao(ocorrencia, agora) * FATOR_DIAS;
        NivelRisco nivel = classificar(pontuacao);
        return new AvaliacaoRisco(pontuacao, nivel, nivel.getRotulo());
    }

    public NivelRisco classificar(double pontuacao) {
        if (pontuacao >= LIMITE_ALTO) {
            return NivelRisco.ALTO;
        }
        if (pontuacao >= LIMITE_MEDIO) {
            return NivelRisco.MEDIO;
        }
        return NivelRisco.BAIXO;
    }

    /** Dias (com fracao) entre a criacao da ocorrencia e o momento informado. */
    public double diasDesdeCriacao(Ocorrencia ocorrencia, OffsetDateTime agora) {
        if (ocorrencia.getDataCriacao() == null) {
            return 0.0;
        }
        long milissegundos = Duration.between(ocorrencia.getDataCriacao(), agora).toMillis();
        return Math.max(0.0, milissegundos / MILISSEGUNDOS_POR_DIA);
    }

    /**
     * Ordena por pontuacao decrescente e, no empate, pela mais recente.
     * Devolve as ocorrencias ja com o risco calculado (na mesma ordem).
     */
    public List<OcorrenciaPriorizada> priorizar(List<Ocorrencia> ocorrencias) {
        OffsetDateTime agora = OffsetDateTime.now(clock);
        Comparator<OcorrenciaPriorizada> porPontuacao =
                Comparator.comparingDouble(p -> p.risco().getPontuacao());
        Comparator<OcorrenciaPriorizada> porData = Comparator.comparing(
                p -> p.ocorrencia().getDataCriacao(), Comparator.nullsFirst(Comparator.naturalOrder()));

        return ocorrencias.stream()
                .map(o -> new OcorrenciaPriorizada(o, calcular(o, agora)))
                .sorted(porPontuacao.reversed().thenComparing(porData.reversed()))
                .toList();
    }

    /** Ocorrencia acompanhada do seu risco calculado. */
    public record OcorrenciaPriorizada(Ocorrencia ocorrencia, AvaliacaoRisco risco) {
    }
}
