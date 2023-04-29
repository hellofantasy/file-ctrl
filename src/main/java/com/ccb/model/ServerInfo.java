package com.ccb.model;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 节点服务注册信息
 */
@Getter
@Setter
public class ServerInfo {
    private String serverId;
    private String runHostNameIP;
    private Integer runStatus;
    private String runStatusMsg;
    private String deployTime;
    private Integer enable;
    private String enableMsg;
    private List<TaskVo> taskVos;
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
