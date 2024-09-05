package com.pocket.spring.application.controller;

import com.pocket.spring.application.Util.Utility;
import com.pocket.spring.application.exception.ApiException;
import com.pocket.spring.application.model.UserModel;
import com.pocket.spring.application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerHandler(@RequestBody UserModel userModel) throws Exception {

        if(!Utility.isValidEmail(userModel.getEmail())){
            throw new ApiException(HttpStatus.BAD_REQUEST,"Mail formatı uygun formatta değil.");
        }

        UserModel loginUserModel = userService.registerUser(userModel);

        return ResponseEntity.ok("User registered successfully with username: " + loginUserModel.getUsername());

    }

    @PostMapping("/login")
    public ResponseEntity<String> loginHandler(@RequestBody UserModel userModel) throws Exception {
        UserModel loginUserModel = userService.loginUser(userModel);
        return ResponseEntity.ok("User logged in successfully with username: " + loginUserModel.getUsername());
    }
}