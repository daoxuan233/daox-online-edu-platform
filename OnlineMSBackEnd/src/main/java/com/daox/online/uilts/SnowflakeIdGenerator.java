package com.daox.online.uilts;

import org.springframework.stereotype.Component;

/**
 * 雪花算法生成id
 */
@Component // 标记为组件，使其成为Spring容器的一个Bean
public class SnowflakeIdGenerator {
    private static final long START_TIMESTAMP = 1691087910202L; // 起始时间戳，可根据实际情况调整

    private static final long DATA_CENTER_ID_BITS = 5L; // 数据中心ID占用的位数
    private static final long WORKER_ID_BITS = 5L; // 工作节点ID占用的位数
    private static final long SEQUENCE_BITS = 12L; // 序列号占用的位数

    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS); // 最大数据中心ID
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS); // 最大工作节点ID
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);   // 最大序列号

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS; // 工作节点ID左移位数
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS; // 数据中心ID左移位数
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS; // 时间戳左移位数

    private final long dataCenterId; // 数据中心ID
    private final long workerId; // 工作节点ID
    private long lastTimestamp = -1L; // 上次生成ID的时间戳
    private long sequence = 0L; // 序列号

    public SnowflakeIdGenerator(){ // 默认构造函数，使用默认数据中心ID和工作节点ID
        this(1, 1);
    }

    private SnowflakeIdGenerator(long dataCenterId, long workerId) { // 构造函数，使用指定的数据中心ID和工作节点ID
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) { // 检查数据中心ID是否超出范围
            throw new IllegalArgumentException("Data center ID can't be greater than " + MAX_DATA_CENTER_ID + " or less than 0");
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) { // 检查工作节点ID是否超出范围
            throw new IllegalArgumentException("Worker ID can't be greater than " + MAX_WORKER_ID + " or less than 0");
        }
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    /**
     * 生成一个新的雪花算法ID加锁
     * @return 雪花ID
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate ID.");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = getNextTimestamp(lastTimestamp); // 获取下一个时间戳
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    private long getCurrentTimestamp() { // 获取当前时间戳
        return System.currentTimeMillis();
    }

    /**
     * 获取下一个时间戳
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 下一个时间戳
     */
    private long getNextTimestamp(long lastTimestamp) {
        long timestamp = getCurrentTimestamp(); // 获取当前时间戳
        while (timestamp <= lastTimestamp) { // 如果当前时间戳小于等于上次生成ID的时间戳，循环等待直到时间戳大于上次生成ID的时间戳
            timestamp = getCurrentTimestamp(); // 获取当前时间戳
        }
        return timestamp;
    }
}
