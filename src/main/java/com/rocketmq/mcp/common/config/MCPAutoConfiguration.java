package com.rocketmq.mcp.common.config;

import com.rocketmq.mcp.common.MQClusterHold;
import com.rocketmq.mcp.infra.entity.Cluster;
import com.rocketmq.mcp.service.MessageService;
import java.time.Duration;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : wh
 * @date : 2025/6/5
 * @description:
 */
@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
public class MCPAutoConfiguration {

    @Bean
    public ToolCallbackProvider myTools(MessageService messageService) {
        return MethodToolCallbackProvider
            .builder()
            .toolObjects(messageService)
            .build();
    }
    
    @Bean
    public MQClusterHold mqClusterHold(RocketMQProperties rocketMQProperties) {
        return new MQClusterHold(rocketMQProperties.getClusters());

    }

    @Bean
    public GenericKeyedObjectPool<Cluster, DefaultMQAdminExt> mqPool() {
        GenericKeyedObjectPoolConfig<DefaultMQAdminExt> genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig<>();
        genericKeyedObjectPoolConfig.setTestWhileIdle(true);
        // 最大对象数
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(8);
        //  最大活跃对象数
        genericKeyedObjectPoolConfig.setMaxIdlePerKey(6);
        // 最小活跃对象数
        genericKeyedObjectPoolConfig.setMinIdlePerKey(1);
        // 最大等待时间
        genericKeyedObjectPoolConfig.setMaxWait(Duration.ofSeconds(15));
        // 空闲检测时间
        genericKeyedObjectPoolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(20));

         genericKeyedObjectPoolConfig.setJmxEnabled(false);
        MQAdminPooledObjectFactory mqAdminPooledObjectFactory = new MQAdminPooledObjectFactory();
        return new GenericKeyedObjectPool<>(
            mqAdminPooledObjectFactory,
            genericKeyedObjectPoolConfig);
    }

}