package com.daox.online.uilts;

import com.daox.online.controller.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON 工具类，用于简化 JSON 字符串和 Java 对象之间的转换。
 */
public class JSONUtil {

    private static final Logger log = LoggerFactory.getLogger(JSONUtil.class);

    // ObjectMapper 是线程安全的，可以作为静态实例共享
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 注册 Java 8 时间模块，以支持 LocalDateTime, LocalDate 等类型的序列化和反序列化
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 将 JSON 字符串转换为指定类型的 Java 对象。
     *
     * @param json  要解析的 JSON 字符串
     * @param clazz 目标对象的 Class 类型
     * @param <T>   目标对象的泛型
     * @return 解析成功后的 Java 对象，如果解析失败则返回 null
     */
    public static <T> T toBean(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            // 在实际应用中，这里应该使用日志框架记录错误
            log.warn("JSON parsing error: {}", e.getMessage());
            throw new BusinessException("400", e.getMessage());
        }
    }

    /**
     * 将 Java 对象转换为 JSON 字符串。
     *
     * @param object 要转换的 Java 对象
     * @return 转换后的 JSON 字符串，如果转换失败则返回 null
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("JSON writing error: {}", e.getMessage());
            throw new BusinessException("400", e.getMessage());
        }
    }
}
