package com.rocketmq.mcp.common.config;

import com.rocketmq.mcp.infra.entity.Cluster;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : wh
 * @date : 2025/6/9
 * @description:
 */
@ConfigurationProperties(prefix = "rocketmq")
@Data
public class RocketMQProperties {

    /**
     * key clusterName
     */
    List<Cluster> clusters;

}
