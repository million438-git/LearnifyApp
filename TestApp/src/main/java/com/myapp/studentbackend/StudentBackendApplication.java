package com.myapp.studentbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Corrected Main Application Class.
 * Class name now matches file name: StudentBackendApplication
 */
@SpringBootApplication
public class StudentBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentBackendApplication.class, args);
    }
}
