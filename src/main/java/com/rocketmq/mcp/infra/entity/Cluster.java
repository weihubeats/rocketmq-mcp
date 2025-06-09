package com.rocketmq.mcp.infra.entity;

import java.util.Objects;
import lombok.Data;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;

/**
 * @author : wh
 * @date : 2025/6/9
 * @description:
 */
@Data
public class Cluster {

    /**
     * 集群id
     */
    private Long id;

    /**
     * 集群名称
     */
    private String name;
    
    private String namesrvAddr;

    private String accessKey;

    private String secretKey;

    private boolean isVIPChannel = false;

    private boolean useTLS = false;
    
    private boolean traceEnabled;

    public boolean isAclEnable() {
        return Objects.nonNull(this.accessKey) && Objects.nonNull(this.secretKey);
    }

    public AclClientRPCHook builderAclClientRPCHook() {
        if (isAclEnable()) {
            return new AclClientRPCHook(new SessionCredentials(
                this.accessKey,
                this.secretKey
            ));
        }
        return null;
    }


}
