package br.com.obracerta.identity;

import br.com.obracerta.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthIntegrationTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    AccountService accounts;

    @BeforeEach
    void createUser() {
        accounts.createUser("Carlos Maineri", "Carlos@Maineri.com.br", "senha-forte-123");
    }

    @Test
    void apiRequiresLogin() {
        assertThat(mvc.get().uri("/api/customers").exchange()).hasStatus(401);
    }

    @Test
    void wrongPasswordIsRejected() {
        MvcTestResult result = login("carlos@maineri.com.br", "senha-errada");

        assertThat(result).hasStatus(401);
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("E-mail ou senha incorretos.");
    }

    @Test
    void loginOpensSessionAndChangesRequireCsrfToken() {
        MvcTestResult result = login("carlos@maineri.com.br", "senha-forte-123");
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Carlos Maineri");

        MockHttpSession session = (MockHttpSession) result.getMvcResult().getRequest().getSession(false);
        Cookie xsrf = result.getMvcResult().getResponse().getCookie("XSRF-TOKEN");
        assertThat(session).isNotNull();
        assertThat(xsrf).isNotNull();

        assertThat(mvc.get().uri("/api/customers").session(session).exchange()).hasStatusOk();

        String body = """
                {"currentPassword":"senha-forte-123","newPassword":"outra-senha-456"}""";
        assertThat(mvc.post().uri("/api/auth/password").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(body).exchange()).hasStatus(403);
        assertThat(mvc.post().uri("/api/auth/password").session(session).cookie(xsrf)
                .header("X-XSRF-TOKEN", xsrf.getValue())
                .contentType(MediaType.APPLICATION_JSON).content(body).exchange()).hasStatus(204);
    }

    @Test
    void shortPasswordAndRepeatedEmailAreRejected() {
        assertThatThrownBy(() -> accounts.createUser("Yuri", "yuri@maineri.com.br", "curta"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> accounts.createUser("Outro", "carlos@maineri.com.br", "senha-forte-123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blocksTheEmailAfterFiveWrongPasswords() {
        accounts.createUser("Bloqueio", "bloqueio@maineri.com.br", "senha-forte-123");
        for (int i = 0; i < LoginThrottle.MAX_FAILURES; i++) {
            assertThat(login("bloqueio@maineri.com.br", "senha-errada")).hasStatus(401);
        }

        MvcTestResult blocked = login("Bloqueio@maineri.com.br", "senha-forte-123");

        assertThat(blocked).hasStatus(429);
        assertThat(blocked).bodyJson().extractingPath("$.detail").asString().startsWith("Muitas tentativas");
        assertThat(login("carlos@maineri.com.br", "senha-forte-123")).hasStatusOk();
    }

    @Test
    void passwordsBeyondTheBcryptLimitAreRejected() {
        assertThat(login("carlos@maineri.com.br", "a".repeat(100))).hasStatus(400);
        assertThatThrownBy(() -> accounts.createUser("Longa", "longa@maineri.com.br", "ç".repeat(37)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anonymousRequestsDoNotOpenASession() {
        MvcTestResult result = mvc.get().uri("/api/customers").exchange();

        assertThat(result).hasStatus(401);
        assertThat(result.getMvcResult().getRequest().getSession(false)).isNull();
    }

    private MvcTestResult login(String email, String password) {
        return mvc.post().uri("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password))
                .exchange();
    }
}
