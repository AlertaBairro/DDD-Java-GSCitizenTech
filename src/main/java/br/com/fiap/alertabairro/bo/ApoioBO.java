package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.ApoioDAO;
import br.com.fiap.alertabairro.dao.OcorrenciaDAO;
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
import java.time.OffsetDateTime;

/**
 * Regras de negocio dos apoios ("eu tambem vi este problema").
 *
 * - somente MORADOR apoia;
 * - um apoio por usuario por ocorrencia (409 se repetir);
 * - nao e possivel apoiar ocorrencia RESOLVIDA (422);
 * - cada apoio soma 1 ao contador e retirar o apoio subtrai 1.
 */
@Service
public class ApoioBO {

    private final ApoioDAO apoioDAO;
    private final OcorrenciaDAO ocorrenciaDAO;
    private final OcorrenciaBO ocorrenciaBO;
    private final UsuarioBO usuarioBO;
    private final Clock clock;

    public ApoioBO(ApoioDAO apoioDAO, OcorrenciaDAO ocorrenciaDAO, OcorrenciaBO ocorrenciaBO,
                   UsuarioBO usuarioBO, Clock clock) {
        this.apoioDAO = apoioDAO;
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.ocorrenciaBO = ocorrenciaBO;
        this.usuarioBO = usuarioBO;
        this.clock = clock;
    }

    public OcorrenciaResponse apoiar(Long ocorrenciaId, Long usuarioId) {
        Usuario morador = usuarioBO.autenticarComPerfil(usuarioId, Perfil.MORADOR, "apoiar ocorrências");
        Ocorrencia ocorrencia = ocorrenciaBO.buscarEntidade(ocorrenciaId);

        if (ocorrencia.isResolvida()) {
            throw new RegraDeNegocioException("Não é possível apoiar uma ocorrência resolvida.");
        }
        if (apoioDAO.existe(morador.getId(), ocorrenciaId)) {
            throw new ConflitoException("Você já apoiou esta ocorrência.");
        }

        apoioDAO.inserir(new Apoio(null, morador.getId(), ocorrenciaId, OffsetDateTime.now(clock)));
        ocorrenciaDAO.alterarQuantidadeApoios(ocorrenciaId, 1);
        ocorrencia.setQuantidadeApoios(ocorrencia.getQuantidadeApoios() + 1);
        return ocorrenciaBO.montarResposta(ocorrencia, true, morador.getId());
    }

    public OcorrenciaResponse retirar(Long ocorrenciaId, Long usuarioId) {
        Usuario morador = usuarioBO.autenticarComPerfil(usuarioId, Perfil.MORADOR, "retirar apoios");
        Ocorrencia ocorrencia = ocorrenciaBO.buscarEntidade(ocorrenciaId);

        if (ocorrencia.isResolvida()) {
            throw new RegraDeNegocioException("Não é possível retirar o apoio de uma ocorrência resolvida.");
        }
        if (ocorrencia.foiRegistradaPor(morador.getId())) {
            throw new RegraDeNegocioException("O apoio de quem registrou a ocorrência não pode ser retirado.");
        }
        if (!apoioDAO.existe(morador.getId(), ocorrenciaId)) {
            throw new RecursoNaoEncontradoException("Você ainda não apoiou esta ocorrência.");
        }

        apoioDAO.excluir(morador.getId(), ocorrenciaId);
        ocorrenciaDAO.alterarQuantidadeApoios(ocorrenciaId, -1);
        ocorrencia.setQuantidadeApoios(Math.max(0, ocorrencia.getQuantidadeApoios() - 1));
        return ocorrenciaBO.montarResposta(ocorrencia, false, morador.getId());
    }
}
