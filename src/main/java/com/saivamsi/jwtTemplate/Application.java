package com.saivamsi.jwtTemplate;

import com.saivamsi.jwtTemplate.model.ApplicationUser;
import com.saivamsi.jwtTemplate.model.Role;
import com.saivamsi.jwtTemplate.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
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
