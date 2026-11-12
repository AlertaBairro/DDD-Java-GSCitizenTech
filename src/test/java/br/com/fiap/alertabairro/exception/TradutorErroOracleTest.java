package br.com.fiap.alertabairro.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class TradutorErroOracleTest {

    private static SQLException ora(int codigo, String mensagem) {
        return new SQLException(mensagem, "23000", codigo);
    }

    @Test
    @DisplayName("ORA-00001 vira 409 com mensagem da constraint de e-mail")
    void uniqueViraConflito() {
        RuntimeException e = TradutorErroOracle.traduzir(
                ora(1, "ORA-00001: unique constraint (RM562272.UK_AB_USUARIO_EMAIL) violated"), "inserir usuário");
        assertThat(e).isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail.");
    }

    @Test
    @DisplayName("ORA-02291 vira 422 (registro pai inexistente)")
    void fkPaiInexistente() {
        RuntimeException e = TradutorErroOracle.traduzir(
                ora(2291, "ORA-02291: integrity constraint (RM562272.FK_AB_OCORRENCIA_TIPO) violated - parent key not found"),
                "inserir ocorrência");
        assertThat(e).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Tipo de ocorrência informado não existe.");
    }

    @Test
    @DisplayName("ORA-02292 vira 422 (existem registros filhos)")
    void fkFilhosExistentes() {
        RuntimeException e = TradutorErroOracle.traduzir(
                ora(2292, "ORA-02292: integrity constraint (RM562272.FK_X_DESCONHECIDA) violated - child record found"),
                "excluir");
        assertThat(e).isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("possui registros vinculados")
                .hasMessageContaining("FK_X_DESCONHECIDA");
    }

    @Test
    @DisplayName("ORA-02290 vira 422 com a mensagem do CHECK")
    void checkViolado() {
        RuntimeException e = TradutorErroOracle.traduzir(
                ora(2290, "ORA-02290: check constraint (RM562272.CK_AB_OCORRENCIA_RESOL) violated"),
                "alterar status");
        assertThat(e).isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("data de resolução");
    }

    @Test
    @DisplayName("Erro encadeado também é reconhecido")
    void erroEncadeado() {
        RuntimeException e = TradutorErroOracle.traduzir(
                new RuntimeException("falha", ora(1, "ORA-00001: unique constraint (PK_AB_APOIO) violated")),
                "apoiar");
        assertThat(e).isInstanceOf(ConflitoException.class).hasMessage("Você já apoiou esta ocorrência.");
    }

    @Test
    @DisplayName("Outros erros viram PersistenciaException (500)")
    void outrosErros() {
        RuntimeException e = TradutorErroOracle.traduzir(ora(17002, "IO Error: connection reset"), "listar bairros");
        assertThat(e).isInstanceOf(PersistenciaException.class).hasMessage("Erro ao listar bairros.");
    }

    @Test
    @DisplayName("Extrai o nome da constraint com e sem esquema")
    void extrairConstraint() {
        assertThat(TradutorErroOracle.extrairConstraint("(RM1.PK_AB_APOIO) violated")).isEqualTo("PK_AB_APOIO");
        assertThat(TradutorErroOracle.extrairConstraint("(PK_AB_APOIO) violated")).isEqualTo("PK_AB_APOIO");
        assertThat(TradutorErroOracle.extrairConstraint("sem constraint")).isNull();
    }
}
