package com.rocketmq.mcp.service;

import com.rocketmq.mcp.common.MQClusterHold;
import com.rocketmq.mcp.infra.entity.Cluster;
import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * @author : wh
 * @date : 2025/6/11
 * @description:
 */
@Service
@RequiredArgsConstructor
public class TopicService {

    private final GenericKeyedObjectPool<Cluster, DefaultMQAdminExt> mqPool;

    private final MQClusterHold mqClusterHold;

    @Tool(description = "创建Topic", name = "创建Topic")
    public void createTopic(@ToolParam(description = "集群名称") String clusterName,
        @ToolParam(description = "topic") String topic,
        @ToolParam(description = "写队列数量", required = false) Integer writeQueueNums
        , @ToolParam(description = "读队列数量", required = false) Integer readQueueNums) throws Exception {

        Cluster cluster = mqClusterHold.getCluster(clusterName);
        DefaultMQAdminExt adminExt = mqPool.borrowObject(cluster);
        ClusterInfo clusterInfo = adminExt.examineBrokerClusterInfo();

        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setTopicName(topic);
        topicConfig.setWriteQueueNums(writeQueueNums);
        topicConfig.setReadQueueNums(readQueueNums);

        clusterInfo.getBrokerAddrTable().forEach((k, v) -> {
            try {
                adminExt.createAndUpdateTopicConfig(v.selectBrokerAddr(), topicConfig);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
