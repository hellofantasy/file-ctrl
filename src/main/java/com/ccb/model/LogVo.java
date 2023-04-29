package com.ccb.model;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LogVo {

    private String logs;

    private List<String> logDateList;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }


}
