package com.saivamsi.remaster;

import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Role;
import com.saivamsi.remaster.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class RemasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemasterApplication.class, args);
    }

    @Bean
    CommandLineRunner run(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            ApplicationUser applicationUser = ApplicationUser.builder()
                    .username("admin")
                    .email("admin@acme.ca")
                    .password(passwordEncoder.encode("password"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(applicationUser);
        };
    }
}
