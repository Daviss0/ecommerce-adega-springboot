package com.adega.adega.repository;

import com.adega.adega.entity.Users;
import com.adega.adega.enumerated.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

Optional<Users> findByEmail(String email);

List<Users> findByRoleIn(List<Role> roles);

List<Users> findByRoleInAndNameContainingIgnoreCase(List<Role> roles, String name);

List<Users> findByRoleInAndEmailContainingIgnoreCase(List<Role> roles, String email);
}
