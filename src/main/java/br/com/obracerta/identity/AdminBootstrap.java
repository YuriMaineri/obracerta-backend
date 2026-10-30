package br.com.obracerta.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Cria o primeiro usuário a partir de OBRACERTA_ADMIN_EMAIL e OBRACERTA_ADMIN_PASSWORD quando não há nenhum. */
@Component
class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AccountService accounts;
    private final String name;
    private final String email;
    private final String password;

    AdminBootstrap(AccountService accounts,
                   @Value("${obracerta.admin.name:Administrador}") String name,
                   @Value("${obracerta.admin.email:}") String email,
                   @Value("${obracerta.admin.password:}") String password) {
        this.accounts = accounts;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (accounts.hasUsers()) {
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            log.warn("Nenhum usuário cadastrado. Defina OBRACERTA_ADMIN_EMAIL e OBRACERTA_ADMIN_PASSWORD e reinicie a API.");
            return;
        }
        accounts.createUser(name, email, password);
        log.info("Primeiro usuário criado: {}", email);
    }
}
