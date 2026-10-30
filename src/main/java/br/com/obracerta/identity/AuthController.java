package br.com.obracerta.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contexts;
    private final AccountService accounts;

    AuthController(AuthenticationManager authenticationManager, SecurityContextRepository contexts,
                   AccountService accounts) {
        this.authenticationManager = authenticationManager;
        this.contexts = contexts;
        this.accounts = accounts;
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@RequestBody @Valid LoginRequest body, HttpServletRequest request,
                            HttpServletResponse response, CsrfToken csrf) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(body.email().trim(), body.password()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."));
        }
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

    record LoginRequest(@NotBlank(message = "Informe o e-mail") String email,
                        @NotBlank(message = "Informe a senha") String password) {
    }

    record PasswordRequest(@NotBlank(message = "Informe a senha atual") String currentPassword,
                           @NotBlank(message = "Informe a nova senha") String newPassword) {
    }

    record UserResponse(Long id, String name, String email) {

        static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail());
        }
    }
}
