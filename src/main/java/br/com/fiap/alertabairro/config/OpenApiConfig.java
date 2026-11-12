package br.com.fiap.alertabairro.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacao OpenAPI / Swagger UI (disponivel em /swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI alertaBairroOpenApi() {
        return new OpenAPI().info(new Info()
                .title("AlertaBairro API")
                .version("1.0.0")
                .description("""
                        API REST do AlertaBairro (Global Solution 2026.2 - FIAP - CitizenTech).
                        "Informe. Previna. Desvie."

                        Zeladoria do bairro + alerta de alagamento + rota segura.

                        Como testar: faça POST /api/auth/login e envie o id devolvido no header
                        X-Usuario-Id nas demais requisições. Usuários de exemplo:
                        morador@alertabairro.com / morador123 (MORADOR),
                        agente@alertabairro.com / agente123 (AGENTE),
                        gestor@alertabairro.com / gestor123 (GESTOR).
                        """));
    }
}
