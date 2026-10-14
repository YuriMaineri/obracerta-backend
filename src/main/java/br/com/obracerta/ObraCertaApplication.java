package br.com.obracerta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/** Monólito modular: cada subpacote direto é um módulo do Spring Modulith. */
@Modulithic(systemName = "ObraCerta")
@SpringBootApplication
public class ObraCertaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObraCertaApplication.class, args);
    }
}
