package com.colegio.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class ProjectColegioApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectColegioApplication.class, args);
    }
}