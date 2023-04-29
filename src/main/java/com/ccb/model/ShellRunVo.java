package com.ccb.model;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShellRunVo {


    private Integer code;

    private String msg;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
