package com.store.storeapp.models;

import com.store.storeapp.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class StoreUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String pass;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public StoreUser(String login, String pass, UserRole role) {
        this.login = login;
        this.pass = pass;
        this.role = role;
    }
}
