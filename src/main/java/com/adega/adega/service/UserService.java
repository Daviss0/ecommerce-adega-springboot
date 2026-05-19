package com.adega.adega.service;


import com.adega.adega.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findInternalUsers();

    List<User> searchInternalUsers(String keyword);

    Optional<User> findById(Long id);

    User save(User user);

    void deactivateUser(Long id);

    void activateUser(Long id);
}
