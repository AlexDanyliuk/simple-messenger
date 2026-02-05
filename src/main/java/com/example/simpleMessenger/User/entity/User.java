package com.example.simpleMessenger.User.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fullName;
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Status status;
}
