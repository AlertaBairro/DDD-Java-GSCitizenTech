package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.LoginRequest;
import br.com.fiap.alertabairro.dto.LoginResponse;
import br.com.fiap.alertabairro.dto.UsuarioUpdateRequest;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.NaoAutenticadoException;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.Apoio;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import br.com.fiap.alertabairro.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.alertabairro.bo.Dados.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UsuarioBO - login, permissões e conta")
class UsuarioBOTest {

    @Mock private UsuarioDAO usuarioDAO;
    @Mock private ApoioDAO apoioDAO;
    @Mock private OcorrenciaDAO ocorrenciaDAO;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private UsuarioBO usuarioBO;

    @BeforeEach
    void configurar() {
        usuarioBO = new UsuarioBO(usuarioDAO, apoioDAO, ocorrenciaDAO, passwordEncoder);
    }

    @Test
    @DisplayName("nome derivado do e-mail: troca . _ - por espaço e aplica Title Case")
    void derivaNome() {
        assertThat(UsuarioBO.derivarNome("maria.da-silva@gmail.com")).isEqualTo("Maria Da Silva");
        assertThat(UsuarioBO.derivarNome("JOAO_PEDRO@fiap.com.br")).isEqualTo("Joao Pedro");
        assertThat(UsuarioBO.derivarNome("ana@x.com")).isEqualTo("Ana");
    }

    @Test
    @DisplayName("login com e-mail novo cria o usuário com o perfil escolhido e senha em hash")
    void loginCriaUsuario() {
        when(usuarioDAO.buscarPorEmail("nova.moradora@gmail.com")).thenReturn(Optional.empty());
        when(usuarioDAO.inserir(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(50L);
            return u;
        });

        LoginResponse resposta = usuarioBO.login(
                new LoginRequest("  Nova.Moradora@gmail.com ", "segredo1", Perfil.MORADOR));

        assertThat(resposta.id()).isEqualTo(50L);
        assertThat(resposta.nome()).isEqualTo("Nova Moradora");
        assertThat(resposta.perfil()).isEqualTo(Perfil.MORADOR);
        assertThat(resposta.novoUsuario()).isTrue();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioDAO).inserir(captor.capture());
        assertThat(captor.getValue().getSenhaHash()).isNotEqualTo("segredo1").startsWith("$2");
        assertThat(passwordEncoder.matches("segredo1", captor.getValue().getSenhaHash())).isTrue();
    }

    @Test
    @DisplayName("login com senha correta devolve o usuário existente")
    void loginExistente() {
        Usuario u = new Usuario(1L, "Morador", "morador@alertabairro.com",
                passwordEncoder.encode("morador123"), Perfil.MORADOR);
        when(usuarioDAO.buscarPorEmail("morador@alertabairro.com")).thenReturn(Optional.of(u));

        LoginResponse resposta = usuarioBO.login(
                new LoginRequest("morador@alertabairro.com", "morador123", Perfil.MORADOR));

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.novoUsuario()).isFalse();
        verify(usuarioDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("senha errada gera 401")
    void senhaErrada() {
        Usuario u = new Usuario(1L, "Morador", "morador@alertabairro.com",
                passwordEncoder.encode("morador123"), Perfil.MORADOR);
        when(usuarioDAO.buscarPorEmail("morador@alertabairro.com")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> usuarioBO.login(
                new LoginRequest("morador@alertabairro.com", "errada", Perfil.MORADOR)))
                .isInstanceOf(NaoAutenticadoException.class);
    }

    @Test
    @DisplayName("e-mail existente com outro perfil gera 403")
    void perfilDiferente() {
        Usuario u = new Usuario(1L, "Morador", "morador@alertabairro.com",
                passwordEncoder.encode("morador123"), Perfil.MORADOR);
        when(usuarioDAO.buscarPorEmail("morador@alertabairro.com")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> usuarioBO.login(
                new LoginRequest("morador@alertabairro.com", "morador123", Perfil.AGENTE)))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("e-mail em formato inválido e senha vazia geram 400")
    void validacoesDoLogin() {
        assertThatThrownBy(() -> usuarioBO.login(new LoginRequest("sem-arroba", "x1234", Perfil.MORADOR)))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> usuarioBO.login(new LoginRequest("a@b.com", "  ", Perfil.MORADOR)))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("perfil diferente do exigido gera 403")
    void exigePerfil() {
        when(usuarioDAO.buscarPorId(1L)).thenReturn(Optional.of(morador(1)));

        assertThatThrownBy(() -> usuarioBO.autenticarComPerfil(1L, Perfil.GESTOR, "ver os indicadores"))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessageContaining("Gestor");
    }

    @Test
    @DisplayName("sem header X-Usuario-Id gera 401")
    void semHeader() {
        assertThatThrownBy(() -> usuarioBO.autenticar(null))
                .isInstanceOf(NaoAutenticadoException.class);
    }

    @Test
    @DisplayName("usuário não pode alterar a conta de outro (403)")
    void naoAlteraOutraConta() {
        when(usuarioDAO.buscarPorId(1L)).thenReturn(Optional.of(morador(1)));

        assertThatThrownBy(() -> usuarioBO.atualizar(2L, new UsuarioUpdateRequest("Novo", null), 1L))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("excluir a conta desconta os apoios dados em ocorrências de outros moradores")
    void excluirContaAjustaContadores() {
        Usuario u = morador(1);
        when(usuarioDAO.buscarPorId(1L)).thenReturn(Optional.of(u));
        when(ocorrenciaDAO.listarPorUsuario(1L)).thenReturn(List.of(
                ocorrencia(7, SANTANA, LIXO, u, 1, 1, StatusOcorrencia.RECEBIDA)));
        when(apoioDAO.listarPorUsuario(1L)).thenReturn(List.of(
                new Apoio(1L, 1L, 7L, AGORA),     // apoio na propria ocorrencia
                new Apoio(2L, 1L, 8L, AGORA)));   // apoio na ocorrencia de outro morador

        usuarioBO.excluir(1L, 1L);

        verify(ocorrenciaDAO).alterarQuantidadeApoios(8L, -1);
        verify(ocorrenciaDAO, never()).alterarQuantidadeApoios(7L, -1);
        verify(apoioDAO).excluirPorUsuario(1L);
        verify(ocorrenciaDAO).excluirPorUsuario(1L);
        verify(usuarioDAO).excluir(1L);
    }
}
