package com.adega.adega.config;

import com.adega.adega.entity.User;
import com.adega.adega.enumerated.Role;
import com.adega.adega.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer  implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if(userRepository.findByEmail("admin@adega.com").isEmpty()) {
            User admin = new User();

            admin.setName("Adminitrador");
            admin.setEmail("admin@adega.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);

        }
    }
}
