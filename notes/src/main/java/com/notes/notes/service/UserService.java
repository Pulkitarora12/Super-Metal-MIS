package com.notes.notes.service;

import com.notes.notes.entity.User;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    void updateUserRole(Long userId, String roleName);

    void deleteUser(Long userId);

    List<User> getAllUsers();

    User getUserById(Long id);

    User findByUsername(String username);
}
