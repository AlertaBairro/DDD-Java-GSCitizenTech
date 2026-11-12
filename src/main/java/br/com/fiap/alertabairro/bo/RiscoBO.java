package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.model.AvaliacaoRisco;
import br.com.fiap.alertabairro.model.NivelRisco;
import br.com.fiap.alertabairro.model.Ocorrencia;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Priorizacao das ocorrencias para o agente de zeladoria.
 *
 * RESOLVIDA: pontuacao 0, nivel BAIXO, rotulo "Resolvido".
 * Demais: pontuacao = nivelRisco * 2 + qtdApoios + diasDesdeRegistro * 0.8
 * Nivel: >= 16 ALTO | >= 10 MEDIO | abaixo disso BAIXO.
 */
@Service
public class RiscoBO {

    public static final double LIMITE_ALTO = 16.0;
    public static final double LIMITE_MEDIO = 10.0;
    public static final double FATOR_RISCO = 2.0;
    public static final double FATOR_DIAS = 0.8;

    private static final double SEGUNDOS_POR_DIA = 86_400.0;

    private final Clock clock;

    public RiscoBO(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }

    public AvaliacaoRisco calcular(Ocorrencia ocorrencia) {
        return calcular(ocorrencia, agora());
    }

    public AvaliacaoRisco calcular(Ocorrencia ocorrencia, LocalDateTime agora) {
        if (ocorrencia.isResolvida()) {
            return new AvaliacaoRisco(0.0, NivelRisco.BAIXO, AvaliacaoRisco.ROTULO_RESOLVIDO);
        }
        double pontuacao = ocorrencia.getNivelRisco() * FATOR_RISCO
                + ocorrencia.getQuantidadeApoios()
                + diasDesdeRegistro(ocorrencia, agora) * FATOR_DIAS;
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

    /** Dias (com fracao) entre o registro da ocorrencia e o momento informado. */
    public double diasDesdeRegistro(Ocorrencia ocorrencia, LocalDateTime agora) {
        if (ocorrencia.getDataRegistro() == null) {
            return 0.0;
        }
        long segundos = Duration.between(ocorrencia.getDataRegistro(), agora).getSeconds();
        return Math.max(0.0, segundos / SEGUNDOS_POR_DIA);
    }

    /** Calcula o risco de cada ocorrencia, mantendo a ordem recebida. */
    public List<OcorrenciaPriorizada> avaliar(List<Ocorrencia> ocorrencias) {
        LocalDateTime agora = agora();
        return ocorrencias.stream()
                .map(o -> new OcorrenciaPriorizada(o, calcular(o, agora)))
                .toList();
    }

    /** Ordena por pontuacao decrescente e, no empate, pela mais recente. */
    public List<OcorrenciaPriorizada> priorizar(List<Ocorrencia> ocorrencias) {
        Comparator<OcorrenciaPriorizada> porPontuacao =
                Comparator.comparingDouble(p -> p.risco().getPontuacao());
        Comparator<OcorrenciaPriorizada> porData = Comparator.comparing(
                p -> p.ocorrencia().getDataRegistro(), Comparator.nullsFirst(Comparator.naturalOrder()));
        return avaliar(ocorrencias).stream()
                .sorted(porPontuacao.reversed().thenComparing(porData.reversed()))
                .toList();
    }

    /** Ocorrencia acompanhada do seu risco calculado. */
    public record OcorrenciaPriorizada(Ocorrencia ocorrencia, AvaliacaoRisco risco) {
    }
}
