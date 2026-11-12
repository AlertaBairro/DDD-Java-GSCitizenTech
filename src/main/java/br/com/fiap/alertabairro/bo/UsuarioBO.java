package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.UsuarioDAO;
import br.com.fiap.alertabairro.dto.LoginRequest;
import br.com.fiap.alertabairro.dto.LoginResponse;
import br.com.fiap.alertabairro.dto.ResumoResponse;
import br.com.fiap.alertabairro.dto.UsuarioCadastroRequest;
import br.com.fiap.alertabairro.dto.UsuarioResponse;
import br.com.fiap.alertabairro.dto.UsuarioUpdateRequest;
import br.com.fiap.alertabairro.exception.AcessoNegadoException;
import br.com.fiap.alertabairro.exception.ConflitoException;
import br.com.fiap.alertabairro.exception.NaoAutenticadoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.exception.ValidacaoException;
import br.com.fiap.alertabairro.model.Bairro;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Regras de usuario: cadastro, login, autenticacao pelo header X-Usuario-Id,
 * permissoes por perfil, atualizacao e exclusao logica da propria conta.
 */
@Service
public class UsuarioBO {

    static final String MSG_CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos.";

    private final UsuarioDAO usuarioDAO;
    private final BairroDAO bairroDAO;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UsuarioBO(UsuarioDAO usuarioDAO, BairroDAO bairroDAO, PasswordEncoder passwordEncoder, Clock clock) {
        this.usuarioDAO = usuarioDAO;
        this.bairroDAO = bairroDAO;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    // ------------------------------------------------------------------
    // Cadastro e login
    // ------------------------------------------------------------------

    /** Cadastro publico: e-mail unico, senha em BCrypt, bairro obrigatorio. O usuario entra ativo. */
    public UsuarioResponse cadastrar(UsuarioCadastroRequest req) {
        if (req == null) {
            throw new ValidacaoException("Informe os dados do cadastro.");
        }
        String email = normalizarEmail(req.email());
        if (usuarioDAO.buscarPorEmail(email).isPresent()) {
            throw new ConflitoException("Já existe um usuário cadastrado com este e-mail.");
        }
        Bairro bairro = exigirBairro(req.bairroId());

        Usuario usuario = new Usuario(null, bairro.getId(), req.nome().trim(), email,
                passwordEncoder.encode(req.senha()), textoOpcional(req.telefone()), req.perfil(),
                LocalDateTime.now(clock), true);
        usuarioDAO.inserir(usuario);
        return montarResposta(usuario, bairro);
    }

    /** Autentica um usuario existente e ativo. Nao cria contas. */
    public LoginResponse login(LoginRequest req) {
        if (req == null || req.email() == null || req.senha() == null) {
            throw new ValidacaoException("Informe e-mail e senha.");
        }
        Usuario usuario = usuarioDAO.buscarPorEmail(normalizarEmail(req.email()))
                .orElseThrow(() -> new NaoAutenticadoException(MSG_CREDENCIAIS_INVALIDAS));
        if (!senhaConfere(req.senha(), usuario.getSenhaHash())) {
            throw new NaoAutenticadoException(MSG_CREDENCIAIS_INVALIDAS);
        }
        if (!usuario.isAtivo()) {
            throw new NaoAutenticadoException("Esta conta foi desativada.");
        }
        return new LoginResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(),
                usuario.getPerfil().getDescricao(), resumoBairro(usuario.getIdBairro()),
                "Login realizado. Envie o header X-Usuario-Id: " + usuario.getId() + " nas próximas requisições.");
    }

    // ------------------------------------------------------------------
    // Autenticacao e permissoes
    // ------------------------------------------------------------------

    /** Usuario do header X-Usuario-Id: precisa existir e estar ativo. */
    public Usuario autenticar(Long idUsuario) {
        if (idUsuario == null) {
            throw new NaoAutenticadoException("Faça login e envie o header X-Usuario-Id com o id do usuário.");
        }
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario)
                .orElseThrow(() -> new NaoAutenticadoException(
                        "Usuário do header X-Usuario-Id não encontrado. Faça login novamente."));
        if (!usuario.isAtivo()) {
            throw new NaoAutenticadoException("Esta conta foi desativada.");
        }
        return usuario;
    }

    /** Autentica e exige um dos perfis informados (403 caso contrario). */
    public Usuario autenticarComPerfil(Long idUsuario, Perfil... perfis) {
        Usuario usuario = autenticar(idUsuario);
        exigirPerfil(usuario, perfis);
        return usuario;
    }

    public void exigirPerfil(Usuario usuario, Perfil... perfis) {
        for (Perfil perfil : perfis) {
            if (usuario.temPerfil(perfil)) {
                return;
            }
        }
        String permitidos = Arrays.stream(perfis).map(Perfil::getDescricao).collect(Collectors.joining(" ou "));
        throw new AcessoNegadoException("Ação permitida apenas para: " + permitidos + ".");
    }

    // ------------------------------------------------------------------
    // Propria conta
    // ------------------------------------------------------------------

    public UsuarioResponse buscar(Long idConta, Long idUsuarioHeader) {
        Usuario usuario = exigirPropriaConta(idConta, idUsuarioHeader, "consultar");
        return montarResposta(usuario, bairroDAO.buscarPorId(usuario.getIdBairro()).orElse(null));
    }

    /** Atualiza nome, telefone, bairro e (opcionalmente) a senha da propria conta. */
    public UsuarioResponse atualizar(Long idConta, Long idUsuarioHeader, UsuarioUpdateRequest req) {
        if (req == null) {
            throw new ValidacaoException("Informe os dados para atualizar.");
        }
        Usuario usuario = exigirPropriaConta(idConta, idUsuarioHeader, "alterar");
        Bairro bairro = exigirBairro(req.bairroId());

        usuario.setNome(req.nome().trim());
        usuario.setTelefone(textoOpcional(req.telefone()));
        usuario.setIdBairro(bairro.getId());
        if (req.senha() != null && !req.senha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(req.senha()));
        }
        usuarioDAO.atualizar(usuario);
        return montarResposta(usuario, bairro);
    }

    /**
     * Exclusao logica da propria conta (st_ativo = 'N'): o usuario e
     * referenciado por varias FKs, entao o registro permanece no banco,
     * mas a conta deixa de logar.
     */
    public void excluir(Long idConta, Long idUsuarioHeader) {
        Usuario usuario = exigirPropriaConta(idConta, idUsuarioHeader, "excluir");
        usuarioDAO.desativar(usuario.getId());
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Usuario exigirPropriaConta(Long idConta, Long idUsuarioHeader, String acao) {
        Usuario usuario = autenticar(idUsuarioHeader);
        if (!usuario.getId().equals(idConta)) {
            throw new AcessoNegadoException("Você só pode " + acao + " a sua própria conta.");
        }
        return usuario;
    }

    private Bairro exigirBairro(Long idBairro) {
        if (idBairro == null) {
            throw new ValidacaoException("Informe o bairro.");
        }
        return bairroDAO.buscarPorId(idBairro)
                .orElseThrow(() -> new RegraDeNegocioException("Bairro informado não existe."));
    }

    private boolean senhaConfere(String senha, String hash) {
        try {
            return hash != null && passwordEncoder.matches(senha, hash);
        } catch (IllegalArgumentException e) {
            return false; // hash em formato invalido (ex.: dados ficticios do DML)
        }
    }

    private ResumoResponse resumoBairro(Long idBairro) {
        return bairroDAO.buscarPorId(idBairro)
                .map(b -> new ResumoResponse(b.getId(), b.getNome()))
                .orElse(new ResumoResponse(idBairro, null));
    }

    private UsuarioResponse montarResposta(Usuario u, Bairro bairro) {
        ResumoResponse resumoBairro = bairro != null
                ? new ResumoResponse(bairro.getId(), bairro.getNome())
                : new ResumoResponse(u.getIdBairro(), null);
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getTelefone(), u.getPerfil(),
                u.getPerfil().getDescricao(), resumoBairro, u.getDataCadastro(), u.isAtivo());
    }

    static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
