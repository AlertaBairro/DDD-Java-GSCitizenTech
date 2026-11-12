package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.BairroDAO;
import br.com.fiap.alertabairro.dao.TipoProblemaDAO;
import br.com.fiap.alertabairro.dto.BairroResponse;
import br.com.fiap.alertabairro.dto.OpcaoResponse;
import br.com.fiap.alertabairro.dto.ReferenciasResponse;
import br.com.fiap.alertabairro.dto.TipoProblemaResponse;
import br.com.fiap.alertabairro.model.Perfil;
import br.com.fiap.alertabairro.model.StatusOcorrencia;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Dados de referencia publicos: bairros, tipos (com peso), status, perfis,
 * ponto de referencia do mapa e raio das zonas de risco.
 */
@Service
public class ReferenciaBO {

    public static final String PONTO_REFERENCIA_NOME = "Estação Jardim São Paulo-Ayrton Senna";
    public static final double PONTO_REFERENCIA_LATITUDE = -23.4923125;
    public static final double PONTO_REFERENCIA_LONGITUDE = -46.6171875;

    private final BairroDAO bairroDAO;
    private final TipoProblemaDAO tipoProblemaDAO;

    public ReferenciaBO(BairroDAO bairroDAO, TipoProblemaDAO tipoProblemaDAO) {
        this.bairroDAO = bairroDAO;
        this.tipoProblemaDAO = tipoProblemaDAO;
    }

    public ReferenciasResponse obterReferencias() {
        return new ReferenciasResponse(
                bairroDAO.listarTodos().stream().map(BairroResponse::de).toList(),
                tipoProblemaDAO.listarTodos().stream().map(TipoProblemaResponse::de).toList(),
                Arrays.stream(StatusOcorrencia.values())
                        .map(s -> new OpcaoResponse(s.name(), s.getDescricao())).toList(),
                Arrays.stream(Perfil.values())
                        .map(p -> new OpcaoResponse(p.name(), p.getDescricao())).toList(),
                new ReferenciasResponse.PontoReferencia(PONTO_REFERENCIA_NOME,
                        PONTO_REFERENCIA_LATITUDE, PONTO_REFERENCIA_LONGITUDE),
                RotaBO.RAIO_ZONA_METROS);
    }
}
