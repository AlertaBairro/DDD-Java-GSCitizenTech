package br.com.fiap.alertabairro.model;

/**
 * Resultado do calculo de risco de uma ocorrencia (objeto de valor,
 * nao persistido): pontuacao, nivel e rotulo exibido ao agente.
 */
public class AvaliacaoRisco {

    public static final String ROTULO_RESOLVIDO = "Resolvido";

    private final double pontuacao;
    private final NivelRisco nivel;
    private final String rotulo;

    public AvaliacaoRisco(double pontuacao, NivelRisco nivel, String rotulo) {
        this.pontuacao = pontuacao;
        this.nivel = nivel;
        this.rotulo = rotulo;
    }

    public double getPontuacao() {
        return pontuacao;
    }

    public NivelRisco getNivel() {
        return nivel;
    }

    public String getRotulo() {
        return rotulo;
    }

    @Override
    public String toString() {
        return "AvaliacaoRisco{pontuacao=" + pontuacao + ", nivel=" + nivel + ", rotulo='" + rotulo + "'}";
    }
}
