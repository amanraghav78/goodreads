package com.Goodreads.UserService.repositories;

import com.Goodreads.UserService.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
