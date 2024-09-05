package com.pocket.spring.application.service;

import com.pocket.spring.application.Util.Constants.*;
import com.pocket.spring.application.Util.Utility;
import com.pocket.spring.application.exception.ApiException;
import com.pocket.spring.application.model.UserModel;
import com.pocket.spring.application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserModel registerUser(UserModel user) throws Exception {

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT,"Username is already taken");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT,"Email is already registered");
        }
        String username = user.getEmail().split("@")[0];
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEmailVerification(false);
        user.setStatus(Status.ACTIVE);

        return userRepository.save(user);
    }

    @Transactional
    public UserModel loginUser(UserModel user) throws Exception {
        Optional<UserModel> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent() && passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
            existingUser.get().setUpdatedAt(LocalDateTime.now());
            return userRepository.save(existingUser.get());
        } else {
            throw new Exception("Invalid email or password");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UserModel getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
