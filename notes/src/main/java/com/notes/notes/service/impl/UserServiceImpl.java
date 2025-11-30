package com.notes.notes.service.impl;

import com.notes.notes.entity.AppRole;
import com.notes.notes.entity.Role;
import com.notes.notes.entity.User;
import com.notes.notes.entity.VerificationToken;
import com.notes.notes.repository.RoleRepository;
import com.notes.notes.repository.UserRepository;
import com.notes.notes.repository.VerificationTokenRepository;
import com.notes.notes.service.EmailService;
import com.notes.notes.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    VerificationTokenRepository verificationTokenRepository;


    @Override
    public User saveUser(User user) {

        // Assign default role if not set already
        if (user.getRole() == null) {
            Role defaultRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.setRole(defaultRole);
        }

        // Save user
        User savedUser = userRepository.save(user);

        // If user already verified, don't send email
        if (savedUser.isVerified()) {
            return savedUser;
        }

        // Generate UUID token
        String tokenValue = java.util.UUID.randomUUID().toString();

        // Create and save verification token
        VerificationToken token = new VerificationToken();
        token.setToken(tokenValue);
        token.setUser(savedUser);
        token.setExpiryDate(LocalDateTime.now().plusHours(24)); // token valid for 24 hours
        token.setUsed(false);

        verificationTokenRepository.save(token);

        // Send email to admin
        emailService.sendVerificationEmailToAdmin(savedUser, tokenValue);

        return savedUser;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Convert String to Enum (assuming AppRole is your enum)
        AppRole appRole;
        try {
            appRole = AppRole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role name: " + roleName);
        }

        // Now check with enum version
        Role role = roleRepository.findByRoleName(AppRole.valueOf(appRole.name()))
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.setRole(role);
        userRepository.save(user);
    }


    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
}
