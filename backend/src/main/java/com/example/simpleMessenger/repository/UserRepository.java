package com.example.simpleMessenger.repository;


import com.example.simpleMessenger.entity.Status;
import com.example.simpleMessenger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    List<User> findAllByStatus(Status status);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

}
