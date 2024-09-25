package com.Goodreads.UserService.controllers;

import com.Goodreads.UserService.DTOs.UserRegisterDTO;
import com.Goodreads.UserService.entities.User;
import com.Goodreads.UserService.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class userController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterDTO userRegisterDTO){
        User user = userService.register(userRegisterDTO);
        if(user!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body("User successfully registered");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User not registered");
        }
    }

    @GetMapping("/")
    public ResponseEntity<String> test(){
        return ResponseEntity.status(HttpStatus.OK).body("Api working");
    }
}
