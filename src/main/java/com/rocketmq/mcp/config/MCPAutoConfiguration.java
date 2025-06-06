package com.rocketmq.mcp.config;

import com.rocketmq.mcp.service.MessageService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : wh
 * @date : 2025/6/5
 * @description:
 */
@Configuration
public class MCPAutoConfiguration {

    @Bean
    public ToolCallbackProvider myTools(MessageService messageService) {
        return MethodToolCallbackProvider
            .builder()
            .toolObjects(messageService)
            .build();
    }
}