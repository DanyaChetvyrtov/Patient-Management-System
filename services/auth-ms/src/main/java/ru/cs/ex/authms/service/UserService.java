package ru.cs.ex.authms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.cs.ex.authms.exception.exceptions.UserNotFoundException;
import ru.cs.ex.authms.model.User;
import ru.cs.ex.authms.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByEmail(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email);

        if (user.isEmpty())
            throw new UserNotFoundException("User with email " + email + " not found");

        return user.get();
    }
}
