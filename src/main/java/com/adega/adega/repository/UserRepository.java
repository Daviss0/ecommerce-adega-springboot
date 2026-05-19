package com.adega.adega.repository;

import com.adega.adega.entity.User;
import com.adega.adega.enumerated.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

Optional<User> findByEmail(String email);

List<User> findByRoleIn(List<Role> roles);

List<User> findByRoleInAndNameContainingIgnoreCase(List<Role> roles, String name);

List<User> findByRoleInAndEmailContainingIgnoreCase(List<Role> roles, String email);
}
