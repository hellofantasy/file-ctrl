package com.ccb.model;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class RequestDto extends TaskVo {
    private String opration;
    private String taskIds;
    private String serverId;
    private String logDate;
    private MultipartFile file;
    private String hostNameIP;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
