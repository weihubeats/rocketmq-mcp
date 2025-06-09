package com.rocketmq.mcp.service;

import com.rocketmq.mcp.dto.MessageView;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author : wh
 * @date : 2025/6/5
 * @description:
 */
public interface MessageService {

    MessageView queryMessageById(String nameserver, String topic, String messageId, String accessKey,
        String secretKey) throws Exception;

    SendResult sendMessage(@ToolParam(description = "集群名称") String clusterName,
        @ToolParam(description = "topic") String topic,
        @ToolParam(description = "消息内容") String messageBody,
        @ToolParam(description = "消息标签，可选") String tags,
        @ToolParam(description = "消息键，可选") String keys,
        @ToolParam(description = "accessKey") String accessKey,
        @ToolParam(description = "secretKey") String secretKey) throws Exception;
    
}
