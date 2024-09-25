package com.Goodreads.UserService.DTOs;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String email;
    private String name;
    private String password;
}
