package com.adega.adega.service.impl;


import com.adega.adega.entity.User;
import com.adega.adega.enumerated.Role;
import com.adega.adega.repository.UserRepository;
import com.adega.adega.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final List<Role> internalRoles = List.of(Role.ADMIN, Role.EMPLOYEE);

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findInternalUsers() {
        return userRepository.findByRoleIn(internalRoles);
    }

    @Override
    public List<User> searchInternalUsers(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()) {
            return findInternalUsers();
        }

        List<User> byName = userRepository.findByRoleInAndNameContainingIgnoreCase(internalRoles, keyword);
        List<User> byEmail = userRepository.findByRoleInAndEmailContainingIgnoreCase(internalRoles, keyword);

        byName.addAll(byEmail);

        return byName.stream()
                .distinct()
                .toList();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User save(User user) {

        if(user.getId() == null) {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole());
            existingUser.setActive(user.getActive());

            if(user.getPassword() != null && !user.getPassword().isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            return userRepository.save(existingUser);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if(user.getActive() == null) {
            user.setActive(true);
        }
        return userRepository.save(user);
    }


    @Override
    public void activateUser(Long id) {
      User user = userRepository.findById(id)
              .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

      user.setActive(true);
      userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setActive(false);
        userRepository.save(user);
    }
}
