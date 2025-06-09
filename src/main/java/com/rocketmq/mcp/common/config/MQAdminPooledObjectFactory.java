package com.rocketmq.mcp.common.config;

import com.rocketmq.mcp.common.MQAdminFactory;
import com.rocketmq.mcp.infra.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;

/**
 * @author : wh
 * @date : 2025/6/9
 * @description:
 */
@RequiredArgsConstructor
@Slf4j
public class MQAdminPooledObjectFactory implements KeyedPooledObjectFactory<Cluster, DefaultMQAdminExt> {

    @Override
    public PooledObject<DefaultMQAdminExt> makeObject(Cluster cluster) throws Exception {
        return new DefaultPooledObject<>(MQAdminFactory.getInstance(cluster));
    }

    @Override
    public void activateObject(Cluster cluster, PooledObject<DefaultMQAdminExt> object) {

    }

    @Override
    public void destroyObject(Cluster cluster, PooledObject<DefaultMQAdminExt> object) {
        MQAdminExt mqAdmin = object.getObject();
        if (mqAdmin != null) {
            try {
                mqAdmin.shutdown();
            } catch (Exception e) {
                log.error("shutdown MQAdminExt error", e);
            }
        }
        log.info("shutdown MQAdminExt success");

    }



    @Override
    public void passivateObject(Cluster cluster, PooledObject<DefaultMQAdminExt> object) {

    }

    @Override
    public boolean validateObject(Cluster cluster, PooledObject<DefaultMQAdminExt> p) {
        MQAdminExt mqAdmin = p.getObject();
        ClusterInfo clusterInfo = null;
        try {
            clusterInfo = mqAdmin.examineBrokerClusterInfo();
        } catch (Exception e) {
            log.warn("validate object {} err", p.getObject(), e);
        }
        if (clusterInfo == null || MapUtils.isEmpty(clusterInfo.getBrokerAddrTable())) {
            log.warn("validateObject failed, clusterInfo = {}", clusterInfo);
            return false;
        }
        return true;
    }
}
