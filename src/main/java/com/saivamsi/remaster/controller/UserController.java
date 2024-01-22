package com.saivamsi.remaster.controller;

import com.saivamsi.remaster.response.UserResponse;
import com.saivamsi.remaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity
                .ok(userService.loadUserByUsername(username).getSafeUser());
    }
}
