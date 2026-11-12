package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import br.com.fiap.alertabairro.model.TipoProblema;
import br.com.fiap.alertabairro.model.Usuario;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Massa de dados compartilhada pelos testes dos BOs.
 */
final class Dados {

    static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), FUSO);
    static final OffsetDateTime AGORA = OffsetDateTime.now(RELOGIO);

    static final Bairro SANTANA = new Bairro(1L, "Santana");
    static final Bairro JARDIM_SAO_PAULO = new Bairro(2L, "Jardim São Paulo");
    static final Bairro TUCURUVI = new Bairro(3L, "Tucuruvi");

    static final TipoProblema ALAGADO = new TipoProblema("ALAGADO", "Ponto alagado", 5, true);
    static final TipoProblema BUEIRO = new TipoProblema("BUEIRO", "Bueiro entupido", 4, false);
    static final TipoProblema BOCA = new TipoProblema("BOCA", "Boca de lobo danificada", 3, false);
    static final TipoProblema LIXO = new TipoProblema("LIXO", "Lixo acumulado", 2, false);

    private Dados() {
    }

    static Usuario morador(long id) {
        return new Usuario(id, "Morador " + id, "morador" + id + "@alertabairro.com", "hash", Perfil.MORADOR);
    }

    static Usuario agente(long id) {
        return new Usuario(id, "Agente " + id, "agente" + id + "@alertabairro.com", "hash", Perfil.AGENTE);
    }

    static Usuario gestor(long id) {
        return new Usuario(id, "Gestor " + id, "gestor" + id + "@alertabairro.com", "hash", Perfil.GESTOR);
    }

    static Ocorrencia ocorrencia(long id, Bairro bairro, TipoProblema tipo, Usuario autor, double horasAtras,
                                 int apoios, StatusOcorrencia status) {
        OffsetDateTime criacao = AGORA.minusMinutes(Math.round(horasAtras * 60));
        OffsetDateTime resolucao = status == StatusOcorrencia.RESOLVIDA ? AGORA : null;
        return new Ocorrencia(id, bairro, tipo, autor, "Observação " + id, criacao, apoios, status,
                resolucao, null, null);
    }

    static Ocorrencia comLocalizacao(Ocorrencia ocorrencia, double lat, double lng) {
        ocorrencia.setLatitude(lat);
        ocorrencia.setLongitude(lng);
        return ocorrencia;
    }
}
