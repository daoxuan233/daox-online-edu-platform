package com.daox.pptAgent.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtUtils {

    // 从配置文件中注入 JWT 密钥
    @Value("${spring.security.jwt.key}")
    private String key;

    // 从配置文件中注入 JWT 过期时间(h)
    @Value("${spring.security.jwt.expire}")
    private Integer expire;

    // 冷却时间 (s)
    @Value("${spring.security.jwt.limit.base}")
    private int limit_base;

    // 封禁时间
    @Value("${spring.security.jwt.limit.upgrade}")
    private int limit_upgrade;

    // 冷却时间还恶意刷
    @Value("${spring.security.jwt.limit.frequency}")
    private int limit_frequency;

    @Resource
    StringRedisTemplate stringRedisTemplate; // 注入 StringRedisTemplate

    @Resource
    FlowUtils flowUtils;

    /**
     * 频率检测，防止高频申请JWT，同时封禁恶意用户 -> 封禁更长时间
     *
     * @param userId 用户id
     * @return true:通过，false:封禁
     */
    private Boolean frequencyCheck(String userId) {
        String key = Const.JWT_FREQUENCY + userId;
        return flowUtils.limitOnceUpgradeCheck(key, limit_upgrade, limit_base, limit_frequency);
    }

    /**
     * 快速计算过期时间
     *
     * @return 过期时间
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance(); // 获取当前时间的 Calendar 实例
        calendar.add(Calendar.HOUR, expire); // 将当前时间的小时数增加 expire 个单位
        return calendar.getTime(); // 返回增加了 expire 小时后的时间
    }

    /**
     * 解析JWT
     *
     * @param headerToken 请求头中的token
     * @return 解析后的JWT DecodedJWT
     */
    public DecodedJWT resolveJWT(String headerToken) {
        String token = convertToken(headerToken);
        if (token == null || token.isEmpty())
            return null;
        // 解析token
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build(); // 创建一个 JWTVerifier 对象，用于验证 JWT 的签名
        try {
            DecodedJWT verify = verifier.verify(token); // 验证 JWT 的签名
            // 检查Token是否在黑名单中
            if (this.isInvalidToken(verify.getId())) {
                log.warn("Token '{}' 已被拉黑.", verify.getId());
                return null;
            }
            Map<String, Claim> claim = verify.getClaims(); // 获取 JWT 中的所有声明
            // 检查Token是否过期 (虽然verifier.verify已经检查了，但双重检查更保险)
            if (new Date().after(verify.getExpiresAt())) {
                log.warn("Token '{}' 已过期.", verify.getId());
                return null;
            }
            // 验证成功
            return verify;
            /*return new Date().after(claim.get("exp").asDate()) ? null : verify;*/ // 检查 JWT 的过期时间是否在当前时间之后
        } catch (JWTVerificationException e) {
            // 这是最关键的修改：添加详细的错误日志
            log.error("JWT Token 验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验转换请求头中的token
     *
     * @param headerToken 请求头中的token
     * @return 转换后的token
     */
    private String convertToken(String headerToken) {
        if (headerToken == null || !headerToken.startsWith("Bearer ")) { // 判断请求头中的 token 是否为空或不以 "Bearer " 开头
            return null;
        }
        return headerToken.substring(7);// 切割Bearer ,只返回token
    }

    /**
     * 校验token是否有效,是否在redis黑名单
     *
     * @param uuid 令牌id
     * @return true:有效，false:无效
     */
    private Boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.JWT_BLACK_LIST + uuid));
    }

    /**
     * 让令牌失效
     *
     * @param headerToken 请求头中的token
     * @return 是否操作成功
     */
    public Boolean invalidateJwt(String headerToken) {
        String token = convertToken(headerToken);
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build(); // 创建一个 JWTVerifier 对象，用于验证 JWT 的签名
        try {
            DecodedJWT verify = verifier.verify(token);
            return deleteToken(verify.getId(), verify.getExpiresAt()); // 删除令牌
        } catch (JWTVerificationException e) {
            return false;
        }

    }

    /**
     * 删除令牌 --> 黑名单
     *
     * @param uuid 令牌id
     * @param time 过期时间
     * @return 是否操作成功
     */
    public Boolean deleteToken(String uuid, Date time) {
        if (this.isInvalidToken(uuid)) return false; // 检查 JWT 的 ID 是否在黑名单中 ==> 无效
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0); // 计算剩余的过期时间 -> 防止负数
        stringRedisTemplate.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MICROSECONDS); // 将 JWT 的 ID 添加到黑名单中
        return true;
    }

    /**
     * 解析jwt，获取id
     *
     * @param jwt 已解析的Jwt对象
     * @return 用户id
     */
    public String toId(DecodedJWT jwt) {
        Map<String, Claim> claim = jwt.getClaims();
        return claim.get("id").asString();
    }
}
