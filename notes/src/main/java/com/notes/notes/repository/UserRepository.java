package com.notes.notes.repository;

import com.notes.notes.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByUserName(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUserName(String username);

    Optional<User> findByEmail(String adminEmail);
}
