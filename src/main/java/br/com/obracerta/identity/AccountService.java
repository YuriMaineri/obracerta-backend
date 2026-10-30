package br.com.obracerta.identity;

import br.com.obracerta.identity.internal.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

/** Usuários do sistema. O login usa o e-mail; a senha só existe como hash. */
@Service
@Transactional(readOnly = true)
public class AccountService implements UserDetailsService {

    static final int MIN_PASSWORD_LENGTH = 8;

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    AccountService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        AppUser user = users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isActive())
                .roles("USER")
                .build();
    }

    public AppUser findByEmail(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado: " + email));
    }

    public boolean hasUsers() {
        return users.count() > 0;
    }

    @Transactional
    public AppUser createUser(String name, String email, String password) {
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalized)) {
            throw new IllegalArgumentException("Já existe um usuário com o e-mail " + normalized);
        }
        checkStrength(password);
        return users.save(new AppUser(name.trim(), normalized, passwordEncoder.encode(password)));
    }

    @Transactional
    public void recordLogin(String email) {
        findByEmail(email).recordLogin();
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        AppUser user = findByEmail(email);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("A senha atual não confere.");
        }
        checkStrength(newPassword);
        user.changePassword(passwordEncoder.encode(newPassword));
    }

    private static void checkStrength(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("A senha precisa ter pelo menos " + MIN_PASSWORD_LENGTH + " caracteres.");
        }
    }
}
