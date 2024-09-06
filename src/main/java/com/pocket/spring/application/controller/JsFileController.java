package com.pocket.spring.application.controller;

import com.pocket.spring.application.Util.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/js")
public class JsFileController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/getFile")
    public ResponseEntity<?> serveJsFile(@RequestParam String pageId, HttpServletRequest request) {

        String filePath = "src/main/resources/static/js/"+pageId+".js";

        try {
            Path path = Paths.get(filePath);
            byte[] fileBytes = Files.readAllBytes(path);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/javascript")
                    .body(new String(fileBytes));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
