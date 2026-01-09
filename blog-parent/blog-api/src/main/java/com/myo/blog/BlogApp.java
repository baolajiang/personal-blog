package com.myo.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BlogApp {

    public static void main(String[] args) {
        SpringApplication.run(BlogApp.class, args);
    }



}
