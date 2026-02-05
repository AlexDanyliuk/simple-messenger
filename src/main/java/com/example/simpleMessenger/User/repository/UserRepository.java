package com.example.simpleMessenger.User.repository;


import com.example.simpleMessenger.User.entity.Status;
import com.example.simpleMessenger.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    List<User> findAllByStatus(Status status);

    User findByUsername(String username);
}
