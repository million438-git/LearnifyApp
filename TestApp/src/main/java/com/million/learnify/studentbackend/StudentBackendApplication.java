package com.million.learnify.studentbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * The Central Brain of the TestApp AI System.
 * Package: com.example.testapp.studentbackend
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.testapp.studentbackend")
public class StudentBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentBackendApplication.class, args);
    }
}
