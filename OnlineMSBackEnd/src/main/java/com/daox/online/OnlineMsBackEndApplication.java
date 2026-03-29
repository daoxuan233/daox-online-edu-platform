package com.daox.online;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 启用定时任务（分类保留期巡检等）
@EnableAsync       // 启用异步任务支持（分类操作邮件通知异步发送）
@EnableMongoRepositories(basePackages = "com.daox.online.repository.mongodb")
@MapperScan("com.daox.online.mapper")
public class OnlineMsBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineMsBackEndApplication.class, args);
    }

}
