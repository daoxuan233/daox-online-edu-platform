package com.daox.online.uilts;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class HybridIdGenerator {
    // 基础时间戳（2002-10-20 00:00:00）
    private static final long EPOCH = 1288834974657L;

    // 机器 ID 所占位数
    private static final long WORKER_ID_BITS = 10L;
    // 序列号所占位数
    private static final long SEQUENCE_BITS = 12L;

    // 机器 ID 最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    // 序列号最大值
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    // 机器 ID 向左移位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    // 时间戳向左移位数
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    // 工作机器 ID
    private final long workerId;
    // 序列号
    private final AtomicLong sequence = new AtomicLong(0L);
    // 上次生成 ID 的时间戳
    private long lastTimestamp = -1L;

    // 安全随机数生成器，用于 UUID 增强
    private final SecureRandom secureRandom = new SecureRandom();

    // 单例模式
    private static final HybridIdGenerator INSTANCE = new HybridIdGenerator();

    public static HybridIdGenerator getInstance() {
        return INSTANCE;
    }

    private HybridIdGenerator() {
        this.workerId = getWorkerId();
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID 不能大于 " + MAX_WORKER_ID + " 或小于 0");
        }
    }

    /**
     * 生成唯一 ID
     */
    public synchronized String generateId() {
        long currentTimestamp = System.currentTimeMillis();

        // 处理时钟回拨
        if (currentTimestamp < lastTimestamp) {
            // 轻微回拨：等待时钟恢复
            if (lastTimestamp - currentTimestamp < 5) {
                currentTimestamp = waitForNextMillis(lastTimestamp);
            }
            // 严重回拨：使用 UUID 作为备选方案
            else {
                return generateEnhancedUUID();
            }
        }

        if (currentTimestamp == lastTimestamp) {
            long seq = sequence.incrementAndGet() & SEQUENCE_MASK;
            if (seq == 0) {
                // 序列号用尽，等待下一毫秒
                currentTimestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，重置序列号
            sequence.set(0L);
        }

        lastTimestamp = currentTimestamp;

        // 组合雪花算法部分
        long snowflakeId = ((currentTimestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence.get();

        // 组合 UUID 部分
        UUID uuid = UUID.randomUUID();
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        // 最终 ID：雪花算法部分 + UUID 哈希部分
        return String.format("%016x%016x%020d",
                mostSigBits,
                leastSigBits,
                snowflakeId);
    }

    /**
     * 生成增强型 UUID（包含时间和随机数信息）
     */
    private String generateEnhancedUUID() {
        UUID uuid = UUID.randomUUID();
        long timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        long random = secureRandom.nextLong();

        return String.format("%s-%s-%s-%s-%s-%d-%d",
                uuid.getMostSignificantBits() >>> 32,
                (uuid.getMostSignificantBits() >>> 16) & 0xFFFF,
                uuid.getMostSignificantBits() & 0xFFFF,
                uuid.getLeastSignificantBits() >>> 48,
                (uuid.getLeastSignificantBits() >>> 32) & 0xFFFF,
                timestamp,
                random);
    }

    /**
     * 等待下一毫秒
     */
    private long waitForNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 获取机器 ID（基于 MAC 地址和进程 ID）
     */
    private long getWorkerId() {
        try {
            // 获取 MAC 地址哈希
            long macHash = getMacAddressHash();
            // 获取进程 ID
            long pid = getProcessId();

            // 组合生成 workerId
            return (macHash ^ pid) & MAX_WORKER_ID;
        } catch (Exception e) {
            // 如果获取失败，使用随机数
            return (long) (secureRandom.nextDouble() * MAX_WORKER_ID);
        }
    }

    /**
     * 获取 MAC 地址哈希值
     */
    private long getMacAddressHash() throws SocketException, UnknownHostException {
        InetAddress ip = InetAddress.getLocalHost();
        NetworkInterface network = NetworkInterface.getByInetAddress(ip);

        // 如果没有找到网络接口，尝试所有接口
        if (network == null) {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces.hasMoreElements()) {
                network = interfaces.nextElement();
            }
        }

        // 如果仍未找到，返回随机值
        if (network == null) {
            return secureRandom.nextLong();
        }

        byte[] mac = network.getHardwareAddress();
        if (mac == null) {
            return secureRandom.nextLong();
        }

        long hash = 0;
        for (byte b : mac) {
            hash = (hash << 8) | (b & 0xFF);
        }
        return hash;
    }

    /**
     * 获取当前进程 ID
     */
    private long getProcessId() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        if (processName.contains("@")) {
            return Long.parseLong(processName.substring(0, processName.indexOf("@")));
        }
        return secureRandom.nextLong();
    }

    // 示例使用
    public static void main(String[] args) {
        HybridIdGenerator generator = HybridIdGenerator.getInstance();

        // 生成 10 个 ID 进行测试
        for (int i = 0; i < 10; i++) {
            System.out.println(generator.generateId());
        }
    }
}