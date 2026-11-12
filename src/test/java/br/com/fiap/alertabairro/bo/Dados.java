package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.TipoOcorrenciaDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import br.com.fiap.alertabairro.model.TipoOcorrencia;
import br.com.fiap.alertabairro.model.Usuario;
import br.com.fiap.alertabairro.model.Zona;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Dados de teste compartilhados: relogio fixo, usuarios de cada perfil,
 * bairros e tipos do DML do grupo, e um UsuarioBO real com DAOs mockados
 * (para que as regras de autenticacao e perfil sejam as de verdade).
 */
final class Dados {

    static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    /** 12/11/2026 10:00 em Sao Paulo. */
    static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-12T13:00:00Z"), FUSO);
    static final LocalDateTime AGORA = LocalDateTime.now(RELOGIO);

    static final Long MORADOR = 11L;
    static final Long OUTRO_MORADOR = 12L;
    static final Long AGENTE = 13L;
    static final Long GESTOR = 14L;
    static final Long INATIVO = 15L;

    static final Bairro JARDIM_SAO_PAULO = new Bairro(1L, "Jardim São Paulo", Zona.NORTE, "Santana/Tucuruvi");
    static final Bairro SANTANA = new Bairro(2L, "Santana", Zona.NORTE, "Santana/Tucuruvi");

    static final TipoOcorrencia BUEIRO = new TipoOcorrencia(1L, "Bueiro entupido", "Bueiro com folhas", 5);
    static final TipoOcorrencia LIXO = new TipoOcorrencia(3L, "Lixo acumulado", "Lixo na calçada", 3);

    private Dados() {
    }

    static Usuario usuario(Long id, Perfil perfil) {
        return new Usuario(id, 1L, "Usuário " + id, "u" + id + "@alertabairro.com", "hash", null, perfil,
                AGORA.minusDays(30), true);
    }

    /** UsuarioDAO mockado com um usuario de cada perfil (e um inativo). */
    static UsuarioDAO usuarioDAO() {
        UsuarioDAO dao = mock(UsuarioDAO.class);
        Usuario inativo = usuario(INATIVO, Perfil.MORADOR);
        inativo.setAtivo(false);
        List<Usuario> todos = List.of(usuario(MORADOR, Perfil.MORADOR), usuario(OUTRO_MORADOR, Perfil.MORADOR),
                usuario(AGENTE, Perfil.AGENTE), usuario(GESTOR, Perfil.GESTOR), inativo);
        when(dao.buscarPorId(anyLong())).thenReturn(Optional.empty());
        for (Usuario u : todos) {
            when(dao.buscarPorId(u.getId())).thenReturn(Optional.of(u));
        }
        when(dao.buscarNomes(any())).thenReturn(java.util.Map.of());
        return dao;
    }

    static BairroDAO bairroDAO() {
        BairroDAO dao = mock(BairroDAO.class);
        when(dao.listar()).thenReturn(List.of(JARDIM_SAO_PAULO, SANTANA));
        when(dao.buscarPorId(anyLong())).thenReturn(Optional.empty());
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(JARDIM_SAO_PAULO));
        when(dao.buscarPorId(2L)).thenReturn(Optional.of(SANTANA));
        return dao;
    }

    static TipoOcorrenciaDAO tipoDAO() {
        TipoOcorrenciaDAO dao = mock(TipoOcorrenciaDAO.class);
        when(dao.listar()).thenReturn(List.of(BUEIRO, LIXO));
        when(dao.buscarPorId(anyLong())).thenReturn(Optional.empty());
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(BUEIRO));
        when(dao.buscarPorId(3L)).thenReturn(Optional.of(LIXO));
        return dao;
    }

    static PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(4); // custo baixo para os testes rodarem rapido
    }

    static UsuarioBO usuarioBO(UsuarioDAO usuarioDAO, BairroDAO bairroDAO) {
        return new UsuarioBO(usuarioDAO, bairroDAO, encoder(), RELOGIO);
    }

    static Ocorrencia ocorrencia(Long id, Long autor, StatusOcorrencia status, int nivelRisco, int apoios,
                                 LocalDateTime registro) {
        Ocorrencia o = new Ocorrencia();
        o.setId(id);
        o.setIdUsuario(autor);
        o.setIdTipoOcorrencia(1L);
        o.setIdBairro(1L);
        o.setEnderecoReferencia("Av. Leôncio de Magalhães, 100");
        o.setLatitude(-23.4922);
        o.setLongitude(-46.6166);
        o.setDataRegistro(registro);
        o.setNivelRisco(nivelRisco);
        o.setStatus(status);
        o.setQuantidadeApoios(apoios);
        if (status == StatusOcorrencia.RESOLVIDA) {
            o.setDataResolucao(registro.plusDays(1));
        }
        return o;
    }
}
