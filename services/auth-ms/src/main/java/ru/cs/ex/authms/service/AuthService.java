package ru.cs.ex.authms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.cs.ex.authms.dto.LoginRequestDto;
import ru.cs.ex.authms.model.User;
import ru.cs.ex.authms.security.JwtUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public Optional<String> authenticate(LoginRequestDto loginRequestDto) {
        User user = userService.findByEmail(loginRequestDto.getEmail());

        if (!passwordEncoder.matches(user.getPassword(), loginRequestDto.getPassword()))
            throw new BadCredentialsException("Wrong password");

        var token = jwtUtil.generateAccessToken(user.getEmail(), user.getRole());
        return Optional.of(token);
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
