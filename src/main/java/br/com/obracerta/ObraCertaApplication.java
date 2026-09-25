package br.com.obracerta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Monolito modular. Cada subpacote direto deste pacote e um modulo de aplicacao
 * na visao do Spring Modulith. Nao crie classes soltas aqui.
 */
@Modulithic(systemName = "ObraCerta")
@SpringBootApplication
public class ObraCertaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObraCertaApplication.class, args);
    }
}
