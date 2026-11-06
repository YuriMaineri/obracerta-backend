package br.com.obracerta.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contexts;
    private final AccountService accounts;
    private final LoginThrottle throttle;

    AuthController(AuthenticationManager authenticationManager, SecurityContextRepository contexts,
                   AccountService accounts, LoginThrottle throttle) {
        this.authenticationManager = authenticationManager;
        this.contexts = contexts;
        this.accounts = accounts;
        this.throttle = throttle;
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@RequestBody @Valid LoginRequest body, HttpServletRequest request,
                            HttpServletResponse response, CsrfToken csrf) {
        String email = body.email().trim();
        Instant lockedUntil = throttle.lockedUntil(email);
        if (lockedUntil != null) {
            long minutes = Math.max(1, Duration.between(Instant.now(), lockedUntil).toSeconds() / 60 + 1);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                            "Muitas tentativas com senha errada. Tente de novo em " + minutes + " min."));
        }
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, body.password()));
        } catch (AuthenticationException e) {
            throttle.failed(email);
            log.warn("Login recusado para {}", email.replaceAll("[\\r\\n\\t]", "_"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."));
        }
        throttle.succeeded(email);
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        accounts.recordLogin(authentication.getName());
        csrf.getToken();
        return ResponseEntity.ok(UserResponse.from(accounts.findByEmail(authentication.getName())));
    }

    /** Ler o token aqui garante o cookie XSRF-TOKEN para as próximas requisições. */
    @GetMapping("/me")
    UserResponse me(Principal principal, CsrfToken csrf) {
        csrf.getToken();
        return UserResponse.from(accounts.findByEmail(principal.getName()));
    }

    @PostMapping("/password")
    ResponseEntity<Void> changePassword(@RequestBody @Valid PasswordRequest body, Principal principal) {
        accounts.changePassword(principal.getName(), body.currentPassword(), body.newPassword());
        return ResponseEntity.noContent().build();
    }

    /** Senha com no máximo 72 caracteres: o bcrypt só considera os primeiros 72 bytes. */
    record LoginRequest(@NotBlank(message = "Informe o e-mail") @Size(max = 150, message = "E-mail ou senha incorretos.") String email,
                        @NotBlank(message = "Informe a senha") @Size(max = 72, message = "E-mail ou senha incorretos.") String password) {
    }

    record PasswordRequest(@NotBlank(message = "Informe a senha atual") @Size(max = 72, message = "A senha atual não confere.") String currentPassword,
                           @NotBlank(message = "Informe a nova senha") @Size(max = 72, message = "A nova senha pode ter no máximo 72 caracteres.") String newPassword) {
    }

    record UserResponse(Long id, String name, String email) {

        static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail());
        }
    }
}
