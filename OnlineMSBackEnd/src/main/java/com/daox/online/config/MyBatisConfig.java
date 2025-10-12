package com.daox.online.config;

import com.daox.online.handler.JsonMapTypeHandler;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MyBatisConfig {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Bean
    public TypeHandlerRegistry registerTypeHandlers() {
        TypeHandlerRegistry registry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        registry.register(Map.class, new JsonMapTypeHandler());
        return registry;
    }
}
