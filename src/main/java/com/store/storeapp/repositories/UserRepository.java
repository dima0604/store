package com.store.storeapp.repositories;

import com.store.storeapp.models.StoreUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<StoreUser, Long> {

    StoreUser findByLogin(String login);

    boolean existsByLogin(String email);
}
