package com.rocketmq.mcp.service;

import com.rocketmq.mcp.dto.MessageView;

/**
 * @author : wh
 * @date : 2025/6/5
 * @description:
 */
public interface MessageService {

    MessageView queryMessageById(String nameserver, String topic, String messageId, String accessKey,
        String secretKey);
    
}
