package com.million.learnify.studentbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * The Central Brain of the TestApp AI System.
 * Package: com.example.testapp.studentbackend
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class StudentBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentBackendApplication.class, args);
    }
}
