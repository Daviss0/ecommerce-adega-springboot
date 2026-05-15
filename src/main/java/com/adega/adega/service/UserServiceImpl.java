package com.adega.adega.service;


import com.adega.adega.entity.Users;
import com.adega.adega.enumerated.Role;
import com.adega.adega.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final List<Role> internalRoles = List.of(Role.ADMIN, Role.EMPLOYEE);

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Users> findInternalUsers() {
        return userRepository.findByRoleIn(internalRoles);
    }

    @Override
    public List<Users> searchInternalUsers(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()) {
            return findInternalUsers();
        }

        List<Users> byName = userRepository.findByRoleInAndNameContainingIgnoreCase(internalRoles, keyword);
        List<Users> byEmail = userRepository.findByRoleInAndEmailContainingIgnoreCase(internalRoles, keyword);

        byName.addAll(byEmail);

        return byName.stream()
                .distinct()
                .toList();
    }

    @Override
    public Optional<Users> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Users save(Users user) {
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if(user.getActive() == null) {
            user.setActive(true);
        }

        return userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long id) {
      Users user = userRepository.findById(id)
              .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

      user.setActive(false);
      userRepository.save(user);
    }


    @Override
    public void activateUser(Long id) {
      Users user = userRepository.findById(id)
              .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

      user.setActive(true);
      userRepository.save(user);
    }
}
