package com.pocket.spring.application.service;

import com.pocket.spring.application.Util.Constants.*;
import com.pocket.spring.application.Util.Utility;
import com.pocket.spring.application.exception.ApiException;
import com.pocket.spring.application.model.User;
import com.pocket.spring.application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User registerUser(User user) {
        String userName=extractUserName(user.getEmail());
        if (userRepository.findByUsername(userName).isPresent()) {
            throw new ApiException.UserNameAlreadyExistsException("Username is already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUsername(userName);
        user.setNameTitle(extractNameTitle(user.getName(),user.getSurname()));
        user.setCreateTime(Utility.getRealTime());
        user.setCreateDate(Utility.getRealDate());
        user.setLastLoginDate("");
        user.setLastLoginTime("");
        user.setStatus(Status.ACTIVE);
        return userRepository.save(user);
    }

    public Entry<UserDetails, UUID> loadUserByUsernameWithId(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UUID userId = user.getId();
        org.springframework.security.core.userdetails.User.UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getUsername());
        builder.password(user.getPassword());
        builder.authorities("USER");
        UserDetails userDetails = builder.build();
        return new AbstractMap.SimpleEntry<>(userDetails, userId);
    }

    @Transactional
    public void updateUserFields(UUID userId, Map<String, Object> updateData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException.UserIdNotFoundException(userId.toString()));
        updateData.forEach((key, value) -> {
            try {
                Field field = User.class.getDeclaredField(key);
                field.setAccessible(true);
                field.set(user, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ApiException.InvalidRequestField(key);
            }
        });
        userRepository.save(user);
    }

    @Transactional
    public void updateUserStatus(UUID userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException.UserIdNotFoundException(userId.toString()));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByUsernameWithId(username).getKey();
    }

    @Transactional
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private String extractNameTitle(String name,String surname) {
        return capitalize(name) + " " + surname.toUpperCase();
    }
    private String extractUserName(String email) {
        String userName = email.split("@")[0];
        if (!userName.isEmpty()) {
            return userName;
        } else {
            throw new ApiException.InvalidEmailFormat(email);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

}