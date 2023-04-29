package com.ccb.model;

import com.alibaba.fastjson.JSON;
import com.ccb.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskVo {
    //任务IF
    private String taskId;
    //任务名
    private String taskName;
    //定时执行时间
    private String cronExpression;
    //脚本路径
    private String shellPath = "";
    //脚本内容
    private String shellContent = "";
    //创建时间
    private String createTime = DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS);
    //上一次执行时间
    private String lastRuntime = "";
    //运行主机IP
    private String runHostNameIP = "";
    //是否可运行
    private Integer enable = 1;
    private String enableMsg = enable == 1 ? "启用" : "禁用";
    //是否一次性任务
    private Integer oneTimeTask = 1; //1 是 0 否
    private String oneTimeTaskMsg = oneTimeTask == 1 ? "是" : "否";
    //运行线程名
    private String threadName;
    //执行状态
    private Integer status = 0;//1 运行中 0 就绪
    private String statusMsg = status == 1 ? "运行中" : "准备就绪";
    //
    private String taskInfoLockFileName;
    //任务信息文件名字
    private String taskInfoFileName;
    //脚本文件名字
    private String runShellFileName;
    //脚本文件锁名字
    private String runShellLockFileName;
    //下一次执行时间
    private String nextRunTime = "";
    //任务组别
    private String groupName = "";
    //任务描述
    private String taskDesc;
    //是否nft推送 0 否 1 是
    private Integer isNftPush = 0;
    private String isNftPushMsg;
    private List<ServerInfo> serverInfos;
    private String targetHost;

    public String getTaskInfoLockFileName() {
        return this.taskId + ".LOCK";
    }

    public String getTaskInfoFileName() {
        return "task-" + this.taskId + ".task";
    }

    public String getRunShellFileName() {
        return "shell-" + this.taskId + ".sh";
    }

    public String getRunShellLockFileName() {
        return "shell-" + this.taskId + ".LOCK";
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
