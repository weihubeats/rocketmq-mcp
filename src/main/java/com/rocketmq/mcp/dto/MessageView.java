package com.rocketmq.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : wh
 * @date : 2025/6/6
 * @description:
 */
@Data
@AllArgsConstructor
public class MessageView {
    
    private  String topic;
    
    private String messageId;
    
    private String messageBody; // body
    
    private String properties; // properties
}
