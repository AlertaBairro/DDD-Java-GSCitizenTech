package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.LoginRequest;
import br.com.fiap.alertabairro.dto.LoginResponse;
import br.com.fiap.alertabairro.dto.UsuarioCadastroRequest;
import br.com.fiap.alertabairro.dto.UsuarioResponse;
import br.com.fiap.alertabairro.dto.UsuarioUpdateRequest;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.ConflitoException;
import br.com.fiap.alertabairro.exception.NaoAutenticadoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioBOTest {

    private UsuarioDAO usuarioDAO;
    private BairroDAO bairroDAO;
    private UsuarioBO bo;

    @BeforeEach
    void preparar() {
        usuarioDAO = Dados.usuarioDAO();
        bairroDAO = Dados.bairroDAO();
        bo = Dados.usuarioBO(usuarioDAO, bairroDAO);
        when(usuarioDAO.buscarPorEmail(anyString())).thenReturn(Optional.empty());
    }

    private Usuario comSenha(String senha, boolean ativo) {
        Usuario u = Dados.usuario(Dados.MORADOR, Perfil.MORADOR);
        u.setSenhaHash(Dados.encoder().encode(senha));
        u.setAtivo(ativo);
        return u;
    }

    // ------------------------------------------------------------------
    // Cadastro
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Cadastro: senha vira BCrypt, e-mail normalizado, usuário entra ativo")
    void cadastroValido() {
        UsuarioCadastroRequest req = new UsuarioCadastroRequest("Maria Silva", " Maria@Email.com ", "1234",
                "11999998888", 1L, Perfil.MORADOR);

        UsuarioResponse resposta = bo.cadastrar(req);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioDAO).inserir(captor.capture());
        Usuario gravado = captor.getValue();
        assertThat(gravado.getEmail()).isEqualTo("maria@email.com");
        assertThat(gravado.getSenhaHash()).startsWith("$2").isNotEqualTo("1234");
        assertThat(Dados.encoder().matches("1234", gravado.getSenhaHash())).isTrue();
        assertThat(gravado.isAtivo()).isTrue();
        assertThat(gravado.getIdBairro()).isEqualTo(1L);
        assertThat(gravado.getDataCadastro()).isEqualTo(Dados.AGORA);
        assertThat(resposta.bairro().nome()).isEqualTo("Jardim São Paulo");
    }

    @Test
    @DisplayName("Cadastro: e-mail já usado dá 409")
    void cadastroEmailDuplicado() {
        when(usuarioDAO.buscarPorEmail("maria@email.com"))
                .thenReturn(Optional.of(Dados.usuario(99L, Perfil.MORADOR)));
        UsuarioCadastroRequest req = new UsuarioCadastroRequest("Maria", "maria@email.com", "1234", null, 1L,
                Perfil.MORADOR);

        assertThatThrownBy(() -> bo.cadastrar(req)).isInstanceOf(ConflitoException.class);
        verify(usuarioDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Cadastro: bairro inexistente dá 422")
    void cadastroBairroInexistente() {
        UsuarioCadastroRequest req = new UsuarioCadastroRequest("Maria", "maria@email.com", "1234", null, 77L,
                Perfil.MORADOR);
        assertThatThrownBy(() -> bo.cadastrar(req)).isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Bairro");
    }

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Login: usuário ativo com senha correta recebe id, perfil e bairro")
    void loginValido() {
        when(usuarioDAO.buscarPorEmail("u11@alertabairro.com")).thenReturn(Optional.of(comSenha("morador123", true)));

        LoginResponse r = bo.login(new LoginRequest("U11@alertabairro.com", "morador123"));

        assertThat(r.id()).isEqualTo(Dados.MORADOR);
        assertThat(r.perfil()).isEqualTo(Perfil.MORADOR);
        assertThat(r.bairro().nome()).isEqualTo("Jardim São Paulo");
    }

    @Test
    @DisplayName("Login: e-mail inexistente dá 401 e não cria usuário")
    void loginNaoCriaUsuario() {
        assertThatThrownBy(() -> bo.login(new LoginRequest("novo@email.com", "1234")))
                .isInstanceOf(NaoAutenticadoException.class);
        verify(usuarioDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Login: senha errada dá 401")
    void loginSenhaErrada() {
        when(usuarioDAO.buscarPorEmail("u11@alertabairro.com")).thenReturn(Optional.of(comSenha("morador123", true)));
        assertThatThrownBy(() -> bo.login(new LoginRequest("u11@alertabairro.com", "errada")))
                .isInstanceOf(NaoAutenticadoException.class).hasMessage(UsuarioBO.MSG_CREDENCIAIS_INVALIDAS);
    }

    @Test
    @DisplayName("Login: usuário inativo (exclusão lógica) não loga")
    void loginUsuarioInativo() {
        when(usuarioDAO.buscarPorEmail("u11@alertabairro.com")).thenReturn(Optional.of(comSenha("morador123", false)));
        assertThatThrownBy(() -> bo.login(new LoginRequest("u11@alertabairro.com", "morador123")))
                .isInstanceOf(NaoAutenticadoException.class).hasMessageContaining("desativada");
    }

    @Test
    @DisplayName("Login: hash fictício do DML não autentica (e não quebra)")
    void loginHashFicticio() {
        Usuario u = Dados.usuario(1L, Perfil.MORADOR);
        u.setSenhaHash("$2b$10$Qm8vK2pLx9TzR4wE6yU1sO");
        when(usuarioDAO.buscarPorEmail("mariana.santos@email.com")).thenReturn(Optional.of(u));
        assertThatThrownBy(() -> bo.login(new LoginRequest("mariana.santos@email.com", "qualquer")))
                .isInstanceOf(NaoAutenticadoException.class);
    }

    @Test
    @DisplayName("Usuários de teste do 03_api_complemento.sql logam com as senhas documentadas")
    void hashesDoScriptConferem() throws IOException {
        String script = Files.readString(Path.of("src/main/resources/db/03_api_complemento.sql"),
                StandardCharsets.UTF_8);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String[][] contas = {
                {"morador@alertabairro.com", "morador123"},
                {"agente@alertabairro.com", "agente123"},
                {"gestor@alertabairro.com", "gestor123"}};
        for (String[] conta : contas) {
            Matcher m = Pattern.compile("'" + Pattern.quote(conta[0]) + "',\\s*'(\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53})'")
                    .matcher(script);
            assertThat(m.find()).as("hash de %s no script", conta[0]).isTrue();
            assertThat(encoder.matches(conta[1], m.group(1))).as("senha de %s", conta[0]).isTrue();
        }
    }

    // ------------------------------------------------------------------
    // Autenticacao, permissoes e propria conta
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Header ausente, usuário inexistente ou inativo dão 401")
    void autenticacao() {
        assertThatThrownBy(() -> bo.autenticar(null)).isInstanceOf(NaoAutenticadoException.class);
        assertThatThrownBy(() -> bo.autenticar(999L)).isInstanceOf(NaoAutenticadoException.class);
        assertThatThrownBy(() -> bo.autenticar(Dados.INATIVO)).isInstanceOf(NaoAutenticadoException.class);
        assertThat(bo.autenticar(Dados.MORADOR).getId()).isEqualTo(Dados.MORADOR);
    }

    @Test
    @DisplayName("Perfil diferente do exigido dá 403")
    void perfilExigido() {
        assertThatThrownBy(() -> bo.autenticarComPerfil(Dados.MORADOR, Perfil.AGENTE))
                .isInstanceOf(AcessoNegadoException.class);
        assertThat(bo.autenticarComPerfil(Dados.GESTOR, Perfil.AGENTE, Perfil.GESTOR).getId()).isEqualTo(Dados.GESTOR);
    }

    @Test
    @DisplayName("Excluir a própria conta é exclusão lógica (st_ativo = 'N')")
    void exclusaoLogica() {
        bo.excluir(Dados.MORADOR, Dados.MORADOR);
        verify(usuarioDAO).desativar(Dados.MORADOR);
    }

    @Test
    @DisplayName("Não é possível excluir, consultar ou alterar a conta de outro usuário (403)")
    void contaDeOutroUsuario() {
        assertThatThrownBy(() -> bo.excluir(Dados.AGENTE, Dados.MORADOR)).isInstanceOf(AcessoNegadoException.class);
        assertThatThrownBy(() -> bo.buscar(Dados.AGENTE, Dados.MORADOR)).isInstanceOf(AcessoNegadoException.class);
        verify(usuarioDAO, never()).desativar(any());
    }

    @Test
    @DisplayName("Atualizar sem nova senha mantém o hash atual; com senha, gera novo hash")
    void atualizarSenhaOpcional() {
        Usuario atual = Dados.usuarioDAO().buscarPorId(Dados.MORADOR).orElseThrow();
        when(usuarioDAO.buscarPorId(Dados.MORADOR)).thenReturn(Optional.of(atual));

        bo.atualizar(Dados.MORADOR, Dados.MORADOR, new UsuarioUpdateRequest("Novo Nome", null, 2L, null));
        assertThat(atual.getSenhaHash()).isEqualTo("hash");
        assertThat(atual.getIdBairro()).isEqualTo(2L);
        assertThat(atual.getNome()).isEqualTo("Novo Nome");

        bo.atualizar(Dados.MORADOR, Dados.MORADOR, new UsuarioUpdateRequest("Novo Nome", null, 2L, "nova123"));
        assertThat(Dados.encoder().matches("nova123", atual.getSenhaHash())).isTrue();
    }
}
