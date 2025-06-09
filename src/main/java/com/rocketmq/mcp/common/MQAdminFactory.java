package com.rocketmq.mcp.common;

import com.rocketmq.mcp.infra.entity.Cluster;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;

/**
 * @author : wh
 * @date : 2025/6/9
 * @description:
 */
@Slf4j
public class MQAdminFactory {

    private static final AtomicLong adminIndex = new AtomicLong(0);

    public static DefaultMQAdminExt getInstance(Cluster cluster) throws Exception {
        DefaultMQAdminExt adminExt;
        if (cluster.isAclEnable()) {
            SessionCredentials credentials = new SessionCredentials(cluster.getAccessKey(),
                cluster.getSecretKey());
            adminExt = new DefaultMQAdminExt(new AclClientRPCHook(credentials), 10000);
        } else {
            adminExt = new DefaultMQAdminExt(10000);
        }
        adminExt.setAdminExtGroup(adminExt.getAdminExtGroup() + adminIndex.incrementAndGet());
        adminExt.setNamesrvAddr(cluster.getNamesrvAddr());
        adminExt.setUseTLS(cluster.isUseTLS());
        adminExt.setVipChannelEnabled(false);
        adminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        adminExt.start();
        return adminExt;

    }
}
