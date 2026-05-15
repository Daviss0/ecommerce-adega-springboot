package com.adega.adega.service;


import com.adega.adega.entity.Users;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<Users> findInternalUsers();

    List<Users> searchInternalUsers(String keyword);

    Optional<Users> findById(Long id);

    Users save(Users user);

    void deactivateUser(Long id);

    void activateUser(Long id);
}
