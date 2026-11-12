package br.com.fiap.alertabairro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Beans de infraestrutura usados pela camada BO.
 */
@Configuration
public class BeansConfig {

    /** Hash de senhas com BCrypt (somente o modulo crypto, sem Spring Security). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Relogio da aplicacao no fuso de Sao Paulo (substituido por um relogio fixo nos testes). */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
