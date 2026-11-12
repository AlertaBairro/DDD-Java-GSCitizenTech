package br.com.fiap.alertabairro.bo;

import br.com.fiap.alertabairro.dao.HealthDAO;
import br.com.fiap.alertabairro.dto.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Verificacao de saude da aplicacao: confirma que a API esta no ar e se
 * conseguiu se conectar ao Oracle.
 */
@Service
public class HealthBO {

    private static final Logger LOG = LoggerFactory.getLogger(HealthBO.class);

    private final HealthDAO healthDAO;
    private final Clock clock;

    public HealthBO(HealthDAO healthDAO, Clock clock) {
        this.healthDAO = healthDAO;
        this.clock = clock;
    }

    public HealthResponse verificar() {
        String banco;
        String mensagem;
        try {
            healthDAO.verificarConexao();
            banco = "UP";
            mensagem = "API no ar e conectada ao banco Oracle.";
        } catch (Exception e) {
            LOG.warn("Health check: falha ao conectar ao banco: {}", e.getMessage());
            banco = "DOWN";
            mensagem = "API no ar, mas sem conexão com o banco: " + causaRaiz(e);
        }
        return new HealthResponse("UP", banco, mensagem, "AlertaBairro API", OffsetDateTime.now(clock));
    }

    private String causaRaiz(Throwable e) {
        Throwable atual = e;
        while (atual.getCause() != null && atual.getCause() != atual) {
            atual = atual.getCause();
        }
        return atual.getMessage() != null ? atual.getMessage() : e.getMessage();
    }
}
