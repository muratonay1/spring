package com.pocket.spring.application.controller;

import com.pocket.spring.application.Util.Constants.*;
import com.pocket.spring.application.Util.JwtTokenProvider;
import com.pocket.spring.application.Util.Utility;
import com.pocket.spring.application.model.User;
import com.pocket.spring.application.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AuthController() {
    }

    // Register
    @Transactional
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user, HttpServletRequest request) throws Exception {
        User userInfo = new User();
        userInfo.setPassword(user.getPassword());
        userService.registerUser(user);
        userInfo.setUsername(user.getUsername());
        return ResponseEntity.ok(createAuthenticationToken(userInfo, request).getBody());
    }

    // Login
    @Transactional(noRollbackFor = BadCredentialsException.class)
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> createAuthenticationToken(@RequestBody User user, HttpServletRequest request) throws Exception {

        Map<String, Object> loginHistoryMap = new HashMap<>();
        Map<String, String> errorResponse = new HashMap<>();
        UUID userId = null;

        try{
            if(user.getUsername().contains("@")){
                user.setUsername(user.getUsername().split("@")[0]);
            }
            User foundUser = userService.findUserByUsername(user.getUsername());
            userId = foundUser.getId();
            loginHistoryMap.put("userId", userId);

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            final String accessToken = jwtTokenProvider.generateAccessToken(foundUser);
            final String refreshToken = jwtTokenProvider.generateRefreshToken(foundUser);


            Map<String, Object> fieldsToUpdate = new HashMap<>();
            fieldsToUpdate.put(GeneralKeys.LAST_LOGIN_DATE, Utility.getRealDate());
            fieldsToUpdate.put(GeneralKeys.LAST_LOGIN_TIME, Utility.getRealTime());

            userService.updateUserFields(userId, fieldsToUpdate);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            return ResponseEntity.ok(tokens);


        }catch (BadCredentialsException e){
            errorResponse.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }


    @Transactional
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshToken) {

        Duration REFRESH_TOKEN_RENEW_THRESHOLD = Duration.ofHours(24);
        String username = jwtTokenProvider.extractUsernameFromRefreshToken(refreshToken.get("refreshToken"));
        if (jwtTokenProvider.validateRefreshToken(refreshToken.get("refreshToken"), username)) {
            User userInfo=userService.findUserByUsername(username);
            String newAccessToken = jwtTokenProvider.generateAccessToken(userInfo);
            if(Duration.between(LocalDateTime.now(),jwtTokenProvider.extractExpirationFromRefreshToken(refreshToken.get("refreshToken")).toInstant().atZone(ZoneId.systemDefault()) .toLocalDateTime()).compareTo(REFRESH_TOKEN_RENEW_THRESHOLD) <= 0){
                String newRefreshToken=jwtTokenProvider.generateRefreshToken(userInfo);
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", newAccessToken);
                tokens.put("refreshToken", newRefreshToken);
                return ResponseEntity.ok(tokens);
            }
            else{
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", newAccessToken);
                tokens.put("refreshToken", refreshToken.get("refreshToken"));
                return ResponseEntity.ok(tokens);
            }

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
        }
    }

    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> accessToken) throws Exception {
        String username = jwtTokenProvider.extractUsername(accessToken.get("accessToken"));
        return ResponseEntity.ok(username + " has logged out.");
    }
}
