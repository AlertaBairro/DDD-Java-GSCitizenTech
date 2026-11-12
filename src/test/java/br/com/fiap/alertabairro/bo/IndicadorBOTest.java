package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.IndicadorDAO;
import br.com.fiap.alertabairro.dao.IndicadorDAO.ReincidenciaBairroTipo;
import br.com.fiap.alertabairro.dao.IndicadorDAO.ReincidenciaPonto;
import br.com.fiap.alertabairro.dao.IndicadorDAO.ResumoBairro;
import br.com.fiap.alertabairro.dao.IndicadorDAO.ResumoTipo;
import br.com.fiap.alertabairro.dao.IndicadorDAO.TotaisAlertas;
import br.com.fiap.alertabairro.dao.IndicadorDAO.TotaisOcorrencias;
import br.com.fiap.alertabairro.dao.IndicadorDAO.TotaisPontosAlagados;
import br.com.fiap.alertabairro.dto.IndicadoresResponse;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndicadorBOTest {

    private IndicadorDAO dao;
    private IndicadorBO bo;

    @BeforeEach
    void preparar() {
        dao = mock(IndicadorDAO.class);
        var usuarioDAO = Dados.usuarioDAO();
        bo = new IndicadorBO(dao, Dados.usuarioBO(usuarioDAO, Dados.bairroDAO()));
    }

    @Test
    @DisplayName("Painel do gestor reúne totais, bairros, tipos, alertas e reincidências")
    void painelCompleto() {
        // Valores do DML do grupo: 8 ocorrencias, 3 resolvidas; 6 pontos (1 ativo); 6 alertas, 13 entregas, 9 lidas
        when(dao.totaisOcorrencias()).thenReturn(new TotaisOcorrencias(8, 5, 106.06));
        when(dao.totaisPontosAlagados()).thenReturn(new TotaisPontosAlagados(6, 1));
        when(dao.totaisAlertas()).thenReturn(new TotaisAlertas(6, 13, 8, 9));
        when(dao.porBairro()).thenReturn(List.of(new ResumoBairro("Jardim São Paulo", 3, 2, 3.7, 4.3)));
        when(dao.porTipo()).thenReturn(List.of(new ResumoTipo("Bueiro entupido", 2, 0)));
        when(dao.reincidenciaPontosAlagados()).thenReturn(List.of(
                new ReincidenciaPonto("Av. Leôncio de Magalhães, em frente à Estação Jardim São Paulo", "Jardim São Paulo", 2)));
        when(dao.reincidenciaBairroTipo()).thenReturn(List.of());

        IndicadoresResponse r = bo.obter(Dados.GESTOR);

        assertThat(r.totalOcorrencias()).isEqualTo(8);
        assertThat(r.ocorrenciasPendentes()).isEqualTo(5);
        assertThat(r.ocorrenciasResolvidas()).isEqualTo(3);
        assertThat(r.tempoMedioResolucaoHoras()).isEqualTo(106.1);
        assertThat(r.tempoMedioResolucaoTexto()).isEqualTo("4 d 10 h");
        assertThat(r.pontosAlagadosAtivos()).isEqualTo(1);
        assertThat(r.pontosAlagadosTotal()).isEqualTo(6);
        assertThat(r.alertasEnviados()).isEqualTo(6);
        assertThat(r.usuariosAlertados()).isEqualTo(8);
        assertThat(r.taxaLeituraPercentual()).isEqualTo(69.2);
        assertThat(r.reincidenciaPontosAlagados()).singleElement()
                .satisfies(p -> assertThat(p.vezes()).isEqualTo(2));
        assertThat(r.porBairro()).hasSize(1);
    }

    @Test
    @DisplayName("Somente GESTOR acessa os indicadores (403)")
    void somenteGestor() {
        assertThatThrownBy(() -> bo.obter(Dados.MORADOR)).isInstanceOf(AcessoNegadoException.class);
        assertThatThrownBy(() -> bo.obter(Dados.AGENTE)).isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("Taxa de leitura: 0 quando nada foi entregue")
    void taxaLeitura() {
        assertThat(IndicadorBO.taxaLeitura(0, 0)).isZero();
        assertThat(IndicadorBO.taxaLeitura(1, 3)).isEqualTo(33.3);
        assertThat(IndicadorBO.taxaLeitura(3, 3)).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Formatação do tempo médio de resolução")
    void formatarHoras() {
        assertThat(IndicadorBO.formatarHoras(null)).isEqualTo("Nenhuma ocorrência resolvida");
        assertThat(IndicadorBO.formatarHoras(0.75)).isEqualTo("45 min");
        assertThat(IndicadorBO.formatarHoras(5.5)).isEqualTo("5,5 h");
        assertThat(IndicadorBO.formatarHoras(50.0)).isEqualTo("2 d 2 h");
    }
}
