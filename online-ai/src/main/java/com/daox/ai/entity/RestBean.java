package com.daox.ai.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * 统一返回数据格式
 * @param <T> 数据类型
 * @param id 请求ID
 * @param code 状态码
 * @param data 数据
 * @param message 消息
 */
public record RestBean<T>(long id , int code , T data , String message) {

    /**
     * 成功响应
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 成功响应
     */
    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(requestId(), 200, data, "请求成功");
    }

    /**
     * 成功响应，无数据
     * @param <T> 响应数据类型
     * @return 成功响应
     */
    public static <T> RestBean<T> success() {
        return success(null);
    }

    /**
     * 失败响应，默认状态码为 403 （禁止访问）
     * @param message 消息
     * @param <T> 响应数据类型
     * @return 失败响应
     */
    public static <T> RestBean<T> forbidden(String message) {
        return failure(403, message);
    }

    /**
     * 失败响应，默认状态码为 401 （未认证）
     * @param message 消息
     * @param <T> 响应数据类型
     * @return 失败响应
     */
    public static <T> RestBean<T> unauthorized(String message) {
        return failure(401, message);
    }

    /**
     * 将当前 RestBean 对象序列化为 JSON 字符串
      * @return JSON 字符串
     */
    public String asJsonString() {
        return JSONObject.toJSONString(this,SerializerFeature.WriteMapNullValue); // 序列化时保留 null 值
    }

    /**
     * 检查响应是否成功
     * @return true 如果响应成功，否则 false
     */
    public boolean isSuccess() {
        return this.code == 200;
    }

    /**
     * 失败响应
     * @param code 状态码
     * @param message 消息
     * @param <T> 响应数据类型
     */
    public static <T> RestBean<T> failure(int code, String message) {
        return new RestBean<>(requestId(), code, null, message);
    }

    /**
     * 失败响应
     * @param code 状态码
     * @param message 消息
     * @param data 响应数据
     * @return 失败响应
     * @param <T> 响应数据类型
     */
    public static <T> RestBean<T> failure(int code, String message, T data) {
        return new RestBean<>(requestId(), code, data, message);
    }


    /**
     * 获取请求ID ，方便定位错误
     * @return ID
     */
    private static long requestId() {
        String requestId = Optional.ofNullable(MDC.get("requestId")).orElse("0"); // 从 MDC 中获取请求ID，如果不存在则使用默认值 "0"
        return Long.parseLong(requestId); // 将请求ID解析为长整型并返回
    }
}
