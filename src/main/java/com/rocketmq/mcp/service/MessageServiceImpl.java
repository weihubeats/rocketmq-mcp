package com.rocketmq.mcp.service;

import com.alibaba.fastjson.JSON;
import com.rocketmq.mcp.dto.MessageView;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * @author : wh
 * @date : 2025/6/6
 * @description:
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    Map<String, DefaultMQAdminExt> defaultMQAdminExts = new HashMap<>();

    private final static int QUERY_MESSAGE_MAX_NUM = 64;

    @Tool(description = "通过topic和消息id查询消息", name = "查询消息")
    @Override
    public MessageView queryMessageById(String nameserver, String topic, String messageId, String accessKey,
        String secretKey) {

        if (Objects.isNull(defaultMQAdminExts.get(nameserver))) {
            DefaultMQAdminExt defaultMQAdminExt = Objects.nonNull(accessKey) && Objects.nonNull(secretKey) ?
                new DefaultMQAdminExt(new AclClientRPCHook(new SessionCredentials(accessKey, secretKey))) : new DefaultMQAdminExt();

            defaultMQAdminExt.setNamesrvAddr(nameserver);
            defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
            try {
                defaultMQAdminExt.start();
            } catch (MQClientException e) {
                throw new RuntimeException(e);
            }
            defaultMQAdminExts.put(nameserver, defaultMQAdminExt);
        }

        DefaultMQAdminExt defaultMQAdminExt = defaultMQAdminExts.get(nameserver);

        QueryResult result;
        try {
            result = defaultMQAdminExt.queryMessageByUniqKey(topic, messageId, QUERY_MESSAGE_MAX_NUM, MessageClientIDSetter.getNearlyTimeFromID(messageId).getTime() - 1000 * 60 * 60 * 13L,
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

}
