package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.LoginRequest;
import br.com.fiap.alertabairro.dto.LoginResponse;
import br.com.fiap.alertabairro.dto.UsuarioResponse;
import br.com.fiap.alertabairro.dto.UsuarioUpdateRequest;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.NaoAutenticadoException;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.Apoio;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Regras de negocio de usuarios: login (com criacao automatica de conta),
 * identificacao pelo header X-Usuario-Id, permissoes por perfil e
 * manutencao da propria conta.
 */
@Service
public class UsuarioBO {

    private static final Pattern FORMATO_EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int TAMANHO_MINIMO_SENHA = 4;
    private static final int TAMANHO_MAXIMO_SENHA = 72;

    private final UsuarioDAO usuarioDAO;
    private final ApoioDAO apoioDAO;
    private final OcorrenciaDAO ocorrenciaDAO;
    private final PasswordEncoder passwordEncoder;

    public UsuarioBO(UsuarioDAO usuarioDAO, ApoioDAO apoioDAO, OcorrenciaDAO ocorrenciaDAO,
                     PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.apoioDAO = apoioDAO;
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    /**
     * Login com e-mail, senha e perfil. Se o e-mail ainda nao existir, a conta
     * e criada com o perfil escolhido (como no prototipo). Se existir, a senha
     * e conferida com o hash BCrypt e o perfil precisa ser o mesmo da conta.
     */
    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            throw new ValidacaoException("Informe e-mail, senha e perfil.");
        }
        String email = normalizarEmail(request.email());
        validarEmail(email);
        if (request.senha() == null || request.senha().isBlank()) {
            throw new ValidacaoException("Informe a senha.");
        }
        if (request.perfil() == null) {
            throw new ValidacaoException("Informe o perfil (MORADOR, AGENTE ou GESTOR).");
        }

        var existente = usuarioDAO.buscarPorEmail(email);
        if (existente.isEmpty()) {
            validarTamanhoSenha(request.senha());
            Usuario novo = new Usuario(null, derivarNome(email), email,
                    passwordEncoder.encode(request.senha()), request.perfil());
            usuarioDAO.inserir(novo);
            return LoginResponse.de(novo, true);
        }

        Usuario usuario = existente.get();
        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new NaoAutenticadoException("E-mail ou senha inválidos.");
        }
        if (usuario.getPerfil() != request.perfil()) {
            throw new AcessoNegadoException("Este e-mail está cadastrado com o perfil "
                    + usuario.getPerfil().getDescricao() + ". Entre com esse perfil.");
        }
        return LoginResponse.de(usuario, false);
    }

    /**
     * Nome derivado do e-mail: parte antes do @, com '.', '_' e '-' trocados
     * por espaco, em Title Case. Ex.: "maria.da-silva@x.com" -> "Maria Da Silva".
     */
    public static String derivarNome(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        String local = email.trim();
        int arroba = local.indexOf('@');
        if (arroba >= 0) {
            local = local.substring(0, arroba);
        }
        return Arrays.stream(local.replaceAll("[._-]", " ").trim().split("\\s+"))
                .filter(parte -> !parte.isEmpty())
                .map(parte -> parte.substring(0, 1).toUpperCase(Locale.ROOT)
                        + parte.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    // ------------------------------------------------------------------
    // Identificacao e permissoes (usadas pelos demais BOs)
    // ------------------------------------------------------------------

    /** Identifica o usuario enviado no header X-Usuario-Id. */
    public Usuario autenticar(Long usuarioId) {
        if (usuarioId == null) {
            throw new NaoAutenticadoException("Faça login e envie o header X-Usuario-Id com o id do usuário.");
        }
        return usuarioDAO.buscarPorId(usuarioId)
                .orElseThrow(() -> new NaoAutenticadoException(
                        "Usuário " + usuarioId + " não encontrado. Faça login novamente."));
    }

    /** Identifica o usuario e exige que ele tenha o perfil informado. */
    public Usuario autenticarComPerfil(Long usuarioId, Perfil perfil, String acao) {
        Usuario usuario = autenticar(usuarioId);
        exigirPerfil(usuario, perfil, acao);
        return usuario;
    }

    public void exigirPerfil(Usuario usuario, Perfil perfil, String acao) {
        if (!usuario.temPerfil(perfil)) {
            throw new AcessoNegadoException("Somente o perfil " + perfil.getDescricao() + " pode " + acao + ".");
        }
    }

    // ------------------------------------------------------------------
    // CRUD da propria conta
    // ------------------------------------------------------------------

    public UsuarioResponse buscar(Long id, Long usuarioLogadoId) {
        autenticar(usuarioLogadoId);
        return UsuarioResponse.de(buscarEntidade(id));
    }

    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request, Long usuarioLogadoId) {
        autenticar(usuarioLogadoId);
        exigirPropriaConta(id, usuarioLogadoId, "alterar");
        Usuario usuario = buscarEntidade(id);

        String nome = request == null || request.nome() == null ? null : request.nome().trim();
        String senha = request == null ? null : request.senha();
        boolean temNome = nome != null && !nome.isEmpty();
        boolean temSenha = senha != null && !senha.isBlank();
        if (!temNome && !temSenha) {
            throw new ValidacaoException("Informe o novo nome e/ou a nova senha.");
        }
        if (temNome) {
            if (nome.length() < 2 || nome.length() > 120) {
                throw new ValidacaoException("O nome deve ter entre 2 e 120 caracteres.");
            }
            usuario.setNome(nome);
        }
        if (temSenha) {
            validarTamanhoSenha(senha);
            usuario.setSenhaHash(passwordEncoder.encode(senha));
        }
        usuarioDAO.atualizar(usuario);
        return UsuarioResponse.de(usuario);
    }

    /**
     * Exclui a propria conta. Antes, desconta os apoios que o usuario deu em
     * ocorrencias de outros moradores; depois remove os apoios, as ocorrencias
     * que ele registrou (com os apoios delas) e o usuario.
     */
    public void excluir(Long id, Long usuarioLogadoId) {
        autenticar(usuarioLogadoId);
        exigirPropriaConta(id, usuarioLogadoId, "excluir");
        buscarEntidade(id);

        Set<Long> ocorrenciasProprias = ocorrenciaDAO.listarPorUsuario(id).stream()
                .map(Ocorrencia::getId)
                .collect(Collectors.toSet());
        for (Apoio apoio : apoioDAO.listarPorUsuario(id)) {
            if (!ocorrenciasProprias.contains(apoio.getOcorrenciaId())) {
                ocorrenciaDAO.alterarQuantidadeApoios(apoio.getOcorrenciaId(), -1);
            }
        }
        apoioDAO.excluirPorUsuario(id);
        ocorrenciaDAO.excluirPorUsuario(id);
        usuarioDAO.excluir(id);
    }

    public Usuario buscarEntidade(Long id) {
        return usuarioDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado."));
    }

    // ------------------------------------------------------------------
    // Validacoes auxiliares
    // ------------------------------------------------------------------

    private void exigirPropriaConta(Long id, Long usuarioLogadoId, String acao) {
        if (!usuarioLogadoId.equals(id)) {
            throw new AcessoNegadoException("Você só pode " + acao + " a sua própria conta.");
        }
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validarEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new ValidacaoException("Informe o e-mail.");
        }
        if (!FORMATO_EMAIL.matcher(email).matches()) {
            throw new ValidacaoException("Informe um e-mail válido.");
        }
    }

    private void validarTamanhoSenha(String senha) {
        if (senha.length() < TAMANHO_MINIMO_SENHA || senha.length() > TAMANHO_MAXIMO_SENHA) {
            throw new ValidacaoException("A senha deve ter entre " + TAMANHO_MINIMO_SENHA + " e "
                    + TAMANHO_MAXIMO_SENHA + " caracteres.");
        }
    }
}
