package com.daox.online;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 启用定时任务
@EnableMongoRepositories(basePackages = "com.daox.online.repository.mongodb")
@MapperScan("com.daox.online.mapper")
public class OnlineMsBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineMsBackEndApplication.class, args);
    }

}
