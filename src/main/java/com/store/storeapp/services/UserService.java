package com.store.storeapp.services;

import com.store.storeapp.UserRole;
import com.store.storeapp.models.StoreUser;
import com.store.storeapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public StoreUser findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Transactional
    public boolean addUser(String login, String pass, UserRole role) {
        if (userRepository.existsByLogin(login)) {
            return false;
        }
        StoreUser user = new StoreUser(login, pass, role);
        userRepository.save(user);

        return true;
    }

}
