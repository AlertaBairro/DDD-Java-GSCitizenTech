package br.com.fiap.alertabairro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * Ponto de entrada da API do AlertaBairro.
 * "Informe. Previna. Desvie."
 */
@SpringBootApplication
public class AlertaBairroApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(AlertaBairroApplication.class, args);
    }
}
