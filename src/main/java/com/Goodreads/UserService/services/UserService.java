package com.Goodreads.UserService.services;

import com.Goodreads.UserService.DTOs.UserRegisterDTO;
import com.Goodreads.UserService.entities.User;
import com.Goodreads.UserService.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User register(UserRegisterDTO userRegisterDTO){
        User user= new User();
        user.setEmail(userRegisterDTO.getEmail());
        user.setName(userRegisterDTO.getName());
        user.setPassword(userRegisterDTO.getPassword());

        return userRepository.save(user);

    }
}
