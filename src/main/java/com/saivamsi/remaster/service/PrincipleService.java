package com.saivamsi.remaster.service;

import com.saivamsi.remaster.exception.GlobalError;
import com.saivamsi.remaster.exception.GlobalException;
import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrincipleService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public ApplicationUser loadUserByUsername(String usernameOrEmail) throws GlobalException {
        Optional<ApplicationUser> user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);

        if (user.isEmpty()) {
            throw new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("user").message("user not found").build());
        }

        return user.get();
    }

}
