package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dto.OcorrenciaResponse;
import br.com.fiap.alertabairro.exception.ConflitoException;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.exception.RegraDeNegocioException;
import br.com.fiap.alertabairro.model.Apoio;
import br.com.fiap.alertabairro.model.Ocorrencia;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Apoios ("eu tambem vi") dos moradores:
 * um por usuario (PK composta), nunca na propria ocorrencia e nunca numa
 * ocorrencia RESOLVIDA. A quantidade de apoios e contada em T_AB_APOIO.
 */
@Service
public class ApoioBO {

    private final ApoioDAO apoioDAO;
    private final OcorrenciaBO ocorrenciaBO;
    private final UsuarioBO usuarioBO;
    private final Clock clock;

    public ApoioBO(ApoioDAO apoioDAO, OcorrenciaBO ocorrenciaBO, UsuarioBO usuarioBO, Clock clock) {
        this.apoioDAO = apoioDAO;
        this.ocorrenciaBO = ocorrenciaBO;
        this.usuarioBO = usuarioBO;
        this.clock = clock;
    }

    public OcorrenciaResponse apoiar(Long idOcorrencia, Long idUsuario) {
        Usuario morador = usuarioBO.autenticarComPerfil(idUsuario, Perfil.MORADOR);
        Ocorrencia o = ocorrenciaBO.buscarEntidade(idOcorrencia);

        if (o.foiRegistradaPor(morador.getId())) {
            throw new RegraDeNegocioException("Você não pode apoiar a sua própria ocorrência.");
        }
        if (o.isResolvida()) {
            throw new RegraDeNegocioException("Não é possível apoiar uma ocorrência já resolvida.");
        }
        if (apoioDAO.existe(idOcorrencia, morador.getId())) {
            throw new ConflitoException("Você já apoiou esta ocorrência.");
        }
        apoioDAO.inserir(new Apoio(idOcorrencia, morador.getId(), LocalDateTime.now(clock)));
        return ocorrenciaBO.montarResposta(ocorrenciaBO.buscarEntidade(idOcorrencia), morador);
    }

    public OcorrenciaResponse retirar(Long idOcorrencia, Long idUsuario) {
        Usuario morador = usuarioBO.autenticarComPerfil(idUsuario, Perfil.MORADOR);
        Ocorrencia o = ocorrenciaBO.buscarEntidade(idOcorrencia);

        if (o.isResolvida()) {
            throw new RegraDeNegocioException("Não é possível retirar o apoio de uma ocorrência já resolvida.");
        }
        if (!apoioDAO.excluir(idOcorrencia, morador.getId())) {
            throw new RecursoNaoEncontradoException("Você não apoiou esta ocorrência.");
        }
        return ocorrenciaBO.montarResposta(ocorrenciaBO.buscarEntidade(idOcorrencia), morador);
    }
}
