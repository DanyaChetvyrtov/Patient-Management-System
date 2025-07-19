package ru.cs.ex.authms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.cs.ex.authms.dto.LoginRequestDto;
import ru.cs.ex.authms.model.User;
import ru.cs.ex.authms.security.JwtUtil;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public void create(LoginRequestDto loginRequestDto) {
        var user = new User(
                null,
                loginRequestDto.getEmail(),
                passwordEncoder.encode(loginRequestDto.getPassword()),
                "ADMIN"
        );
        user = userService.createUser(user);
        log.info("Created user: {}", user);
    }

    public Optional<String> authenticate(LoginRequestDto loginRequestDto) {
        User user = userService.findByEmail(loginRequestDto.getEmail());

        var encryptedPass = passwordEncoder.encode(loginRequestDto.getPassword());
        log.info("Request: {}", loginRequestDto);
        log.info("Encrypted password: {}", encryptedPass);
        log.info("Authenticated user: {}", user);

        if (user.getPassword().equals(encryptedPass))
            throw new BadCredentialsException("Wrong password");

        var token = jwtUtil.generateAccessToken(user.getEmail(), user.getRole());
        return Optional.of(token);
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
