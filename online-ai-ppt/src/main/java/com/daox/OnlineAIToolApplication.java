package com.daox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // 开启异步功能
@SpringBootApplication
public class OnlineAIToolApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineAIToolApplication.class, args);
    }
}
