package com.daox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Hello world!
 * 在线智能体应用
 * 该应用提供在线智能体功能，包括智能体的创建、管理、调用等
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.daox.ai.repository") // <-- 显式指定Repository所在的包
public class OnlineArtificialIntelligenceApplication
{
    public static void main( String[] args ) {
        SpringApplication.run(OnlineArtificialIntelligenceApplication.class, args);
    }
}
