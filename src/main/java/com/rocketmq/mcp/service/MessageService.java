package com.rocketmq.mcp.service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.rocketmq.mcp.common.MQClusterHold;
import com.rocketmq.mcp.dto.MessageView;
import com.rocketmq.mcp.infra.entity.Cluster;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * @author : wh
 * @date : 2025/6/6
 * @description:
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final GenericKeyedObjectPool<Cluster, DefaultMQAdminExt> mqPool;

    private final MQClusterHold mqClusterHold;

    private final static int QUERY_MESSAGE_MAX_NUM = 64;

    @Tool(description = "通过topic和消息id查询消息", name = "查询消息")
    public MessageView queryMessageById(@ToolParam(description = "集群名称") String clusterName,
        @ToolParam(description = "topic") String topic,
        @ToolParam(description = "消息id") String messageId) throws Exception {

        Cluster cluster = mqClusterHold.getCluster(clusterName);
        DefaultMQAdminExt adminExt = mqPool.borrowObject(cluster);

        QueryResult result;
        try {
            result = adminExt.queryMessageByUniqKey(topic, messageId, QUERY_MESSAGE_MAX_NUM, MessageClientIDSetter.getNearlyTimeFromID(messageId).getTime() - 1000 * 60 * 60 * 13L,
                Long.MAX_VALUE);
        } catch (MQClientException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!result.getMessageList().isEmpty()) {
            MessageExt ext = result.getMessageList().getFirst();
            return new MessageView(ext.getTopic(), messageId, new String(ext.getBody(), StandardCharsets.UTF_8), JSON.toJSONString(ext.getProperties()));
        } else {
            return null;
        }
    }

    @Tool(description = "发送消息到指定的topic", name = "发送消息")
    public SendResult sendMessage(@ToolParam(description = "集群名称") String clusterName,
        @ToolParam(description = "topic") String topic,
        @ToolParam(description = "消息内容") String messageBody,
        @ToolParam(description = "消息标签，可选", required = false) String tags,
        @ToolParam(description = "消息键，可选", required = false) String keys) {
        Cluster cluster = mqClusterHold.getCluster(clusterName);
        DefaultMQProducer producer;

        producer = new DefaultMQProducer("ProducerGroup_" + UUID.randomUUID().toString().substring(0, 6));
        producer.setNamesrvAddr(cluster.getNamesrvAddr());

        producer = buildDefaultMQProducer(MixAll.SELF_TEST_PRODUCER_GROUP, cluster);
        try {
            producer.start();
            Message msg = new Message(topic,
                tags,
                keys,
                messageBody.getBytes()
            );
            return producer.send(msg);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        } finally {
            producer.shutdown();
        }

    }

    public DefaultMQProducer buildDefaultMQProducer(String producerGroup, Cluster cluster) {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer(producerGroup, cluster.builderAclClientRPCHook(), cluster.isTraceEnabled(), TopicValidator.RMQ_SYS_TRACE_TOPIC);
        defaultMQProducer.setUseTLS(cluster.isUseTLS());
        defaultMQProducer.setNamesrvAddr(cluster.getNamesrvAddr());
        return defaultMQProducer;
    }

}
