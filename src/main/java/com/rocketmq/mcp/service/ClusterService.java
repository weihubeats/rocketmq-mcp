package com.rocketmq.mcp.service;

import com.google.common.collect.Maps;
import com.rocketmq.mcp.common.MQClusterHold;
import com.rocketmq.mcp.infra.entity.Cluster;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * @author : wh
 * @date : 2025/6/10
 * @description:
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterService {

    private final GenericKeyedObjectPool<Cluster, DefaultMQAdminExt> mqPool;

    private final MQClusterHold mqClusterHold;

    @Tool(description = "查询集群信息", name = "查询集群信息")
    public Map<String, Object> test(@ToolParam(description = "集群名称") String clusterName) throws Exception {
        Cluster cluster = mqClusterHold.getCluster(clusterName);
        DefaultMQAdminExt mqAdminExt = mqPool.borrowObject(cluster);
        Map<String, Object> resultMap = Maps.newHashMap();
        ClusterInfo clusterInfo = mqAdminExt.examineBrokerClusterInfo();
        Map<String/*brokerName*/, Map<Long/* brokerId */, Object/* brokerDetail */>> brokerServer = Maps.newHashMap();
        for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
            Map<Long, Object> brokerMasterSlaveMap = Maps.newHashMap();
            for (Map.Entry<Long/* brokerId */, String/* broker address */> brokerAddr : brokerData.getBrokerAddrs().entrySet()) {
                KVTable kvTable = mqAdminExt.fetchBrokerRuntimeStats(brokerAddr.getValue());
                brokerMasterSlaveMap.put(brokerAddr.getKey(), kvTable.getTable());
            }
            brokerServer.put(brokerData.getBrokerName(), brokerMasterSlaveMap);
        }
        resultMap.put("clusterInfo", clusterInfo);
        resultMap.put("brokerServer", brokerServer);
        return resultMap;
    }
}
