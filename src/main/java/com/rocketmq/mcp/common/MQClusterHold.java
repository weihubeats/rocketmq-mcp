package com.rocketmq.mcp.common;

import com.rocketmq.mcp.infra.entity.Cluster;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : wh
 * @date : 2025/6/9
 * @description:
 */
public class MQClusterHold {

    private final Map<String, Cluster> clusterMap;

    public MQClusterHold(List<Cluster> clusters) {
        this.clusterMap = clusters.stream().collect(Collectors.toMap(Cluster::getName, Function.identity()));
    }

    public Cluster getCluster(String name) {
        return clusterMap.get(name);

    }

}
