package com.daox.pptAgent.model;

import com.daox.pptAgent.service.PptToolbox;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 存储在Redis中的用户会话状态。
 * 这是一个“热”数据对象，用于在对话进行中时频繁读写。
 * 必须实现 Serializable 接口。
 */
@Data // 使用Lombok简化Getter/Setter代码
public class SessionState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // 序列化版本号

    /**
     * 用户已经批准的PPT大纲
     */
    private PptToolbox.PptOutline approvedOutline;

    /**
     * 会话历史记录
     */
    private List<ChatMessage> history = new ArrayList<>();

    /**
     * 会话创建时间
     */
    private final LocalDateTime createTime;

    /**
     * 构造函数<br/>
     * 初始化创建时间
     */
    public SessionState() {
        this.createTime = LocalDateTime.now();
    }

    /**
     * 添加一条消息<br/>方便添加消息到历史记录
     *
     * @param role    消息角色
     * @param content 消息内容
     */
    public void addMessage(String role, String content) {
        this.history.add(new ChatMessage(role, content));
    }
}