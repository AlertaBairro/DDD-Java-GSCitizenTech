package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.TipoOcorrenciaDAO;
import br.com.fiap.alertabairro.dto.BairroResponse;
import br.com.fiap.alertabairro.dto.TipoOcorrenciaResponse;
import br.com.fiap.alertabairro.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Consultas publicas dos cadastros basicos: bairros e tipos de ocorrencia
 * (usados nos formularios de cadastro, registro e filtros do front).
 */
@Service
public class CatalogoBO {

    private final BairroDAO bairroDAO;
    private final TipoOcorrenciaDAO tipoOcorrenciaDAO;

    public CatalogoBO(BairroDAO bairroDAO, TipoOcorrenciaDAO tipoOcorrenciaDAO) {
        this.bairroDAO = bairroDAO;
        this.tipoOcorrenciaDAO = tipoOcorrenciaDAO;
    }

    public List<BairroResponse> listarBairros() {
        return bairroDAO.listar().stream().map(BairroResponse::de).toList();
    }

    public BairroResponse buscarBairro(Long id) {
        return bairroDAO.buscarPorId(id)
                .map(BairroResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bairro não encontrado."));
    }

    public List<TipoOcorrenciaResponse> listarTipos() {
        return tipoOcorrenciaDAO.listar().stream().map(TipoOcorrenciaResponse::de).toList();
    }

    public TipoOcorrenciaResponse buscarTipo(Long id) {
        return tipoOcorrenciaDAO.buscarPorId(id)
                .map(TipoOcorrenciaResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de ocorrência não encontrado."));
    }
}
