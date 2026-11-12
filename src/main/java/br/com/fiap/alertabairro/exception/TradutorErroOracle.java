package br.com.fiap.alertabairro.exception;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traduz os erros de constraint do Oracle em excecoes da aplicacao, para que
 * a API responda 409/422 com uma mensagem clara em vez de 500:
 *
 * <ul>
 *   <li>ORA-00001 (UNIQUE/PK violada) -> {@link ConflitoException} (409)</li>
 *   <li>ORA-02291 (FK: registro pai inexistente) -> {@link RegraDeNegocioException} (422)</li>
 *   <li>ORA-02292 (FK: existem registros filhos) -> {@link RegraDeNegocioException} (422)</li>
 *   <li>ORA-02290 (CHECK violado) -> {@link RegraDeNegocioException} (422)</li>
 * </ul>
 *
 * Qualquer outro erro vira {@link PersistenciaException} (500).
 */
public final class TradutorErroOracle {

    public static final int ORA_UNIQUE = 1;
    public static final int ORA_CHECK = 2290;
    public static final int ORA_FK_PAI_INEXISTENTE = 2291;
    public static final int ORA_FK_FILHOS_EXISTENTES = 2292;

    /** Nome da constraint entre parenteses: "(ESQUEMA.NOME_DA_CONSTRAINT)". */
    private static final Pattern CONSTRAINT = Pattern.compile("\\(([\\w$#]+\\.)?([\\w$#]+)\\)");

    /** Mensagens especificas para as constraints do modelo do grupo. */
    private static final Map<String, String> MENSAGENS = Map.ofEntries(
            Map.entry("UK_AB_USUARIO_EMAIL", "Já existe um usuário cadastrado com este e-mail."),
            Map.entry("UK_AB_BAIRRO_NOME", "Já existe um bairro com este nome."),
            Map.entry("UK_AB_TIPO_NOME", "Já existe um tipo de ocorrência com este nome."),
            Map.entry("PK_AB_APOIO", "Você já apoiou esta ocorrência."),
            Map.entry("PK_AB_ALERTA_USUARIO", "Este usuário já recebeu este alerta."),
            Map.entry("FK_AB_USUARIO_BAIRRO", "Bairro informado não existe."),
            Map.entry("FK_AB_OCORRENCIA_BAIRRO", "Bairro informado não existe."),
            Map.entry("FK_AB_PONTO_BAIRRO", "Bairro informado não existe."),
            Map.entry("FK_AB_OCORRENCIA_TIPO", "Tipo de ocorrência informado não existe."),
            Map.entry("FK_AB_OCORRENCIA_USUARIO", "Usuário informado não existe."),
            Map.entry("FK_AB_APOIO_OCORRENCIA", "A ocorrência informada não existe ou possui apoios vinculados."),
            Map.entry("FK_AB_HISTORICO_OCORRENCIA", "A ocorrência informada não existe ou possui histórico de status."),
            Map.entry("FK_AB_ALERTA_PONTO", "O ponto alagado informado não existe ou possui alertas vinculados."),
            Map.entry("FK_AB_ALERTA_USU_ALERTA", "O alerta informado não existe ou possui destinatários."),
            Map.entry("CK_AB_OCORRENCIA_RESOL",
                    "Ocorrência resolvida precisa de data de resolução e as demais não podem tê-la."),
            Map.entry("CK_AB_OCORRENCIA_STATUS", "Status de ocorrência inválido."),
            Map.entry("CK_AB_OCORRENCIA_RISCO", "O nível de risco deve estar entre 1 e 5."),
            Map.entry("CK_AB_OCORRENCIA_LAT", "Latitude deve estar entre -90 e 90."),
            Map.entry("CK_AB_OCORRENCIA_LONG", "Longitude deve estar entre -180 e 180."),
            Map.entry("CK_AB_PONTO_LAT", "Latitude deve estar entre -90 e 90."),
            Map.entry("CK_AB_PONTO_LONG", "Longitude deve estar entre -180 e 180."),
            Map.entry("CK_AB_PONTO_NIVEL", "Nível da água inválido."),
            Map.entry("CK_AB_PONTO_ATIVO", "Ponto encerrado precisa de data de encerramento e o ativo não pode tê-la."),
            Map.entry("CK_AB_HIST_DIFERENTE", "O novo status precisa ser diferente do atual."),
            Map.entry("CK_AB_USUARIO_PERFIL", "Perfil de usuário inválido."),
            Map.entry("CK_AB_ALERTA_LEITURA", "A data de leitura não pode ser anterior ao recebimento."),
            Map.entry("CK_AB_ROTA_PONTOS", "A quantidade de pontos evitados não pode ser negativa."));

    private TradutorErroOracle() {
    }

    /**
     * Converte uma falha de JDBC em excecao da aplicacao.
     *
     * @param erro     excecao recebida do driver (pode estar encadeada)
     * @param operacao descricao curta da operacao, usada na mensagem de erro 500
     */
    public static RuntimeException traduzir(Throwable erro, String operacao) {
        if (erro instanceof ConflitoException || erro instanceof RegraDeNegocioException
                || erro instanceof PersistenciaException) {
            return (RuntimeException) erro;
        }
        SQLException sql = encontrarSqlException(erro);
        if (sql != null) {
            String constraint = extrairConstraint(sql.getMessage());
            switch (sql.getErrorCode()) {
                case ORA_UNIQUE:
                    return new ConflitoException(mensagem(constraint,
                            "Registro duplicado: já existe um registro com estes dados."));
                case ORA_FK_PAI_INEXISTENTE:
                    return new RegraDeNegocioException(mensagem(constraint,
                            "Um registro referenciado não existe."));
                case ORA_FK_FILHOS_EXISTENTES:
                    return new RegraDeNegocioException(mensagem(constraint,
                            "O registro não pode ser removido porque possui registros vinculados."));
                case ORA_CHECK:
                    return new RegraDeNegocioException(mensagem(constraint,
                            "Os dados violam uma regra de validação do banco."));
                default:
                    break;
            }
        }
        return new PersistenciaException("Erro ao " + operacao + ".", erro);
    }

    /** Extrai o nome da constraint da mensagem do Oracle, ou null. */
    static String extrairConstraint(String mensagem) {
        if (mensagem == null) {
            return null;
        }
        Matcher m = CONSTRAINT.matcher(mensagem);
        return m.find() ? m.group(2).toUpperCase(Locale.ROOT) : null;
    }

    private static String mensagem(String constraint, String padrao) {
        if (constraint == null) {
            return padrao;
        }
        return MENSAGENS.getOrDefault(constraint, padrao + " (" + constraint + ")");
    }

    private static SQLException encontrarSqlException(Throwable erro) {
        Throwable atual = erro;
        int profundidade = 0;
        while (atual != null && profundidade++ < 10) {
            if (atual instanceof SQLException sql) {
                return sql;
            }
            atual = atual.getCause();
        }
        return null;
    }
}
