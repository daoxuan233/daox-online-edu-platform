package com.daox.ai.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *  流量控制工具类<br/>
 *  1. 针对单次请求频率限制，请求成功后，在冷却时间内不得再次进行请求<br/>
 *  2. 针对单次频率限制，请求成功之后，在冷却时间之内不得再次进行请求<br/>
 *      如果继续在冷却时间内请求，将限制更长的时间
 *  3. 针对在时间段内多次请求限制，超出频率则封禁一段时间<br/>
 *  4. 针对在时间段内多次请求限制，超出频率则封禁一段时间，并且封禁时间会随着请求次数的增加而增加<br/>
 *      例如：第一次请求，封禁 1 分钟；第二次请求，封禁 2 分钟；第三次请求，封禁 3 分钟，以此类推
 *      这种方式可以有效避免恶意攻击，因为攻击者需要多次请求才能触发封禁时间的增加
 */
@Slf4j
@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    /**
     * 针对单次请求频率限制，请求成功后，在冷却时间内不得再次进行请求
     * @param key 键
     * @param blockTime 限制时间
     * @return 是否通过限流检查
     */
    public Boolean limitOnceCheck(String key, Integer blockTime){
        return this.internalCheck(key,1,blockTime,(overclock) -> false); // false 意味着无论是否超出频率限制，都会认为请求没有通过限流检查
    }

    /**
     * 针对单次频率限制，请求成功之后，在冷却时间之内不得再次进行请求
     * 如果继续在冷却时间内请求，将限制更长的时间
     * @param key 键
     * @param frequency 请求频率
     * @param baseTime 基础限制时间
     * @param upgradeTime 升级限制时间
     * @return 是否通过限流检测
     */
    public Boolean limitOnceUpgradeCheck(String key, Integer frequency ,Integer baseTime,Integer upgradeTime){
        return this.internalCheck(key,frequency,baseTime,(overclock) -> {
            if(overclock) // 如果当前访问超出了频率限制
                stringRedisTemplate.opsForValue().set(key,"1",upgradeTime,TimeUnit.SECONDS); //将 Redis 中 key 对应的值设置为 "1"，并将过期时间更新为 upgradeTime 秒
            return false; // 返回 false，表示当前访问超出了频率限制
        });
    }


    /**
     * 针对在时间段内多次请求限制，超出频率则封禁一段时间
     * @param countKey 计数键
     * @param blockKey 封禁键
     * @param blockTime 封禁时间
     * @param frequency 请求频率
     * @param period 计数周期
     * @return 是否通过限流检查
     */
    public Boolean limitPeriodCheck(String countKey,String blockKey,Integer blockTime , Integer frequency ,Integer period){
        return this.internalCheck(countKey,frequency,period,(overclock) -> {
            if (overclock) // true，则表示当前的访问次数已经超出了预设的频率限制
                stringRedisTemplate.opsForValue().set(blockKey,"",blockTime,TimeUnit.SECONDS);// 这行代码，其作用是在 Redis 中设置一个阻塞键（blockKey）
                // 值为空字符串。这意味着在 blockTime 秒内，与这个 blockKey 相关的操作会被阻塞
            return !overclock;
        });
    }

    /**
     * 流量控制的内部检查
     *      借助 Redis 来记录特定 key 的访问次数，从而判断是否超出了指定的频率限制
     * @param key 标识流量控制的键
     * @param frequency 允许的访问频率
     * @param period 时间周期 单位为秒，指定了频率限制的时间范围。
     * @param action 一个函数式接口 执行特定的操作，根据检查结果（是否超出频率限制）来决定具体行为
     *               限制行为与策略
     * @return 是否通过限流检查
     */
    private Boolean internalCheck(String key , Integer frequency , Integer period , LimitAction action){
        String count = stringRedisTemplate.opsForValue().get(key); //  从 Redis 中获取计数
        if(count != null){ // 若 key 存在
            long value = Optional.ofNullable(stringRedisTemplate.opsForValue().increment(key)).orElse(0L); // 对 key 对应的值进行自增操作
            int c = Integer.parseInt(count);
            if (value != c+1) // 若自增后的值不等于原计数值加 1
                stringRedisTemplate.expire(key,period, TimeUnit.SECONDS); //重新设置 key 的过期时间为 period 秒。
            return action.run(value > frequency); // 用于判断当前的访问次数
        }else { // 若 key 不存在
            stringRedisTemplate.opsForValue().set(key,"1",period, TimeUnit.SECONDS); // 若 key 不存在，就将 key 的值设置为 1，并设置过期时间为 period 秒
            return Boolean.TRUE; // 表示首次访问，未超出频率限制
        }
    }

    /**
     *  限制行为与策略:
     *
     */
    private interface LimitAction{
        /**
         * 执行特定的操作，根据检查结果（是否超出频率限制）来决定具体行为
         * @param overclock
         *      true,表示当前的访问次数已经超出了预设的频率限制；
         *      false,则表示未超出限制。
         * @return 返回值的具体含义取决于实现该接口的代码逻辑，通常可用于表示某种操作是否成功执行
         */
        boolean run(boolean overclock);
    }
}
