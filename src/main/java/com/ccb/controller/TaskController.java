package com.ccb.controller;

import com.ccb.common.CommonUtils;
import com.ccb.common.Constant;
import com.ccb.common.Feedback;
import com.ccb.model.LogVo;
import com.ccb.model.RequestDto;
import com.ccb.model.ServerInfo;
import com.ccb.model.TaskVo;
import com.ccb.util.*;
import com.ccb.util.GU;
import org.quartz.CronExpression;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/task")
public class TaskController {
    public static SimpleDateFormat simpleDateFormat = new
            SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);

    /**
     * 获取任务列表
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/taskIndex")
    public Feedback index(@RequestBody RequestDto requestDto) {
        try {
            //查询所有任务信息
            List<TaskVo> taskInfos = DataUtils.getTaskVoList();
            //任务名条件过滤
            if (GU.isNotNull(requestDto.getTaskName())) {
                taskInfos = taskInfos.stream().filter(s -> s.getTaskName().contains(requestDto.getTaskName())).collect(Collectors.toList());
            }
            //任务状态过滤
            if (GU.isNotNull(requestDto.getEnable())) {
                taskInfos = taskInfos.stream().filter(s -> s.getEnable() == requestDto.getEnable()).collect(Collectors.toList());
            }
            //一次性任务过滤
            if (GU.isNotNull(requestDto.getOneTimeTask())
                    ) {
                taskInfos = taskInfos.stream().filter(s -> s.getOneTimeTask() == requestDto.getOneTimeTask()).collect(Collectors.toList());
            }
            //任务组别过滤
            if (GU.isNotNull(requestDto.getGroupName())) {
                taskInfos = taskInfos.stream().filter(s -> s.getGroupName().contains(requestDto.getGroupName())).collect(Collectors.toList());
            }
            //按新建日期冒泡排序  倒序
            for (int i = 0; i < taskInfos.size() - 1; i++
                    ) {
                for (int j = 0; j < taskInfos.size() - i
                        - 1; j++) {
                    long time1 = simpleDateFormat.parse(taskInfos.get(j).getCreateTime()).getTime();
                    long time2 = simpleDateFormat.parse(taskInfos.get(j + 1).getCreateTime()).getTime();
                    if (time1 <= time2) {
                        TaskVo temp = taskInfos.get(j);
                        taskInfos.set(j, taskInfos.get(j
                                + 1));
                        taskInfos.set(j + 1, temp);
                    }
                }
            }
            return Feedback.success(taskInfos);
        } catch (Exception e) {
            return Feedback.error("获取任务列表失败：" + e.getMessage());
        }
    }

    /**
     * 保存任务信息
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/saveTask")
    public Feedback addTask(@RequestBody RequestDto requestDto) {
        try {
            TaskVo taskVo = new TaskVo();
            //字段判空
            if (GU.isNull(requestDto.getTaskName(), requestDto.getShellContent())) {
                return Feedback.error("必填参数不为空");
            }
            //shell脚本内容校验 一个shell只能一个任务跑
            String[] shellContentsSplit = requestDto.getShellContent().split(" ");
            List<String> shellsStr = new ArrayList<>();
            for (int i = 0; i < shellContentsSplit.length; i++) {
                if (shellContentsSplit[i].trim().contains(".sh")) {
                    shellsStr.add(shellContentsSplit[i]);
                }
            }
            List<TaskVo> taskVoList = DataUtils.getTaskVoList();
            for (int i = 0; i < shellsStr.size(); i++) {
                for (int j = 0; j < taskVoList.size(); j++) {
                    TaskVo taskVo1 = taskVoList.get(j);
                    if (GU.isNull(requestDto.getTaskId())) {
                        if (taskVo1.getShellContent().contains(shellsStr.get(i))) {
                            return Feedback.error("存在执行相同shell的任务，请确认：\n任务名->" + taskVo1.getTaskName() + ",脚本内容->" + shellsStr.get(i));
                        }
                    } else {
                        if (taskVo1.getShellContent().contains(shellsStr.get(i)) && !taskVo1.getTaskId().equals(requestDto.getTaskId())) {
                            return Feedback.error("存在执行相同shell的任务，请确认：\n任务名->" + taskVo1.getTaskName() + ",脚本内容->" + shellsStr.get(i));
                        }
                    }
                }
            }
            //保存任务
            if (GU.isNull(requestDto.getTaskId())) {
                BeanUtils.copyProperties(requestDto, taskVo);
                taskVo.setTaskId(System.currentTimeMillis() + "");
            } else {
                //执行更新
                taskVo = DataUtils.getTaskInfoByTaskId(requestDto.getTaskId());
                taskVo.setTaskName(requestDto.getTaskName());
                taskVo.setCronExpression(requestDto.getCronExpression());
                taskVo.setShellContent(requestDto.getShellContent());
                taskVo.setEnable(requestDto.getEnable());
                taskVo.setOneTimeTask(requestDto.getOneTimeTask());
                taskVo.setGroupName(requestDto.getGroupName());
                taskVo.setTaskDesc(requestDto.getTaskDesc());
                taskVo.setIsNftPush(requestDto.getIsNftPush());
                taskVo.setTargetHost(requestDto.getTargetHost());
                taskVo.setCreateTime(DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS));
            }
            DataUtils.addTaskInfo(taskVo);
            return Feedback.success("任务保存成功!");
        } catch (Exception e) {
            return Feedback.error("保存任务报错："
                    + e.getMessage());
        }
    }

    /**
     * 删除任务
     *
     * @param
     * @return
     */
    @PostMapping(value = "/delTask")
    public Feedback delTask(@RequestBody RequestDto requestDto) {
        try {
            //查询任务信息
            TaskVo taskVo = DataUtils.getTaskInfoByTaskId
                    (requestDto.getTaskId());
            //执行删除
            boolean result = DataUtils.deleteTaskInfo(taskVo);
            if (!result) {
                return Feedback.error("任务删除失败。");
            }
            return Feedback.success("任务删除成功!");
        } catch (Exception e) {
            return Feedback.error("删除任务报错:" +
                    e.getMessage());
        }
    }

    /**
     * 通过任务ID获取任务信息
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/getTask")
    public Feedback getTask(@RequestBody RequestDto requestDto) {
        try {
            //查询任务信息
            TaskVo taskVo = DataUtils.getTaskInfoByTaskId
                    (requestDto.getTaskId());
            if (GU.isNotNull(taskVo)) {
                return Feedback.success(taskVo);
            } else {
                return Feedback.error("任务不存在。");
            }
        } catch (Exception e) {
            return Feedback.error("获取任务信息出错：" + e.getMessage());
        }
    }

    /**
     * 查询任务信息是否重复
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/checkUnique")
    public Feedback checkUnique(@RequestBody RequestDto requestDto) {
        try {
            //没有任务ID的，为第一次新增
            if (GU.isNotNull(requestDto.getTaskName()) &&
                    GU.isNull(requestDto.getTaskId())) {
                List<TaskVo> taskVoList = DataUtils.getTaskVoList();
                for (TaskVo taskVo : taskVoList) {
                    if (requestDto.getTaskName().equals(taskVo.getTaskName())) {
                        return Feedback.error("存在重的任务名称。");
                    }
                }
            }
            //有任务ID的，任务名字不能和本务以外的任务名重复
            if (GU.isNotNull(requestDto.getTaskName()) &&
                    GU.isNotNull(requestDto.getTaskId())) {
                List<TaskVo> taskVoList = DataUtils.getTaskVoList();
                for (int i = 0; i < taskVoList.size(); i++) {
                    TaskVo taskVo = taskVoList.get(i);
                    if (requestDto.getTaskName().equals(taskVo.getTaskName()) && !taskVo.getTaskId().equals(requestDto.getTaskId())) {
                        return Feedback.error("存在重的任务名称。");
                    }
                }
            }
            return Feedback.success("数据合法");
        } catch (Exception e) {
            return Feedback.error("检查唯一性报错" + e.getMessage());
        }
    }

    /**
     * 启动任务
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/startTask")
    public Feedback startTask(@RequestBody RequestDto requestDto) {
        try {
            String taskId = requestDto.getTaskId();
            if (GU.isNull(taskId)) {
                return Feedback.error("任务执行失败：任务ID不为空");
            }
            //获取任务信息
            TaskVo taskVo = DataUtils.getTaskInfoByTaskId
                    (requestDto.getTaskId());
            String taskName = taskVo.getTaskName();
            //任务是否被禁用
            if (taskVo.getEnable() != 1) {
                return Feedback.error(taskName + " 任务执行失败：任务已经禁用。");
            }
            //任务是否已经开启
            if (taskVo.getStatus() == 1) {
                return Feedback.error(taskName + " 任务执行失败：任务执行中，请勿重复启动。");
            }
            //状态设置为运行中
            taskVo.setStatus(1);
            //一次性任务
            if (taskVo.getOneTimeTask() == 1) {
                //执行一次性任务
//                ServerInfo serverInfo = DataUtils.getServerInfo(Constant.getHostNameIP());
//                //当前节点服务是否启用
//                if (serverInfo.getEnable() == 0) {
//                    return Feedback.error("当前节点未启用，请在节点管理进行启用");
//                }
                //设置运行时间
                taskVo.setLastRuntime(DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS));
                //运行节点名
                taskVo.setRunHostNameIP(Constant.LOCAL_HOSTNAME_IP);
                //更新任务信息
                DataUtils.saveTaskInfo(taskVo);
                //执行shell
                ShellUtils.executeShell(taskVo, requestDto
                        .getHostNameIP());
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("时间：").append(DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS)).append(" 操作主机：").append(requestDto.getHostNameIP()).
                        append(" 执行脚本内容：").append(taskVo.getShellContent());
                CommonUtils.writeToFile(Constant.logsPath
                        + "operationLog-" + DateUtils.getDateStr(DateUtils.YYYY_MM_DD) + ".log", stringBuffer.toString(), true);
                return Feedback.success("执行一次性务成功");
            } else {
                //非一次性任务的，值修改任务状态值，由后台定时程序轮循加载
                taskVo.setRunHostNameIP("");
                //本机启用的话，将该任务装载到本节点
                ServerInfo serverInfo = DataUtils.getServerInfo(Constant.LOCAL_HOSTNAME_IP);
                if (serverInfo.getEnable() == 1) {
                    //当前节点执行装载该任务
                    JobUtils.startJobTask(taskVo);
                }
                //更新任务信息
                DataUtils.saveTaskInfo(taskVo);
                return Feedback.success(taskName + " 任务启动成功。");
            }
        } catch (Exception e) {
            return Feedback.error("启动任务报错:" +
                    e.getMessage());
        }
    }

    /**
     * 关停任务
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/stopTask")
    public Feedback stopTask(@RequestBody RequestDto requestDto) {
        try {
            String success = "";
            String fail = "";
            //拆分任务ID
            String[] taskIds = requestDto.getTaskIds().split(",");
            Map<String, TaskVo> taskVoMap = DataUtils.getTaskVoMap();
            for (int i = 0; i < taskIds.length; i++) {
                if (GU.isNull(taskIds[i])) {
                    continue;
                }
                TaskVo taskVo = taskVoMap.get(taskIds[i]);
                String taskName = taskVo.getTaskName();
                //判断是否启动
                if (taskVo.getStatus() == 0) {
                    fail += taskName + " 未启动; ";
                    continue;
                }
                //任务是否可用
                if (taskVo.getEnable() != 1) {
                    fail += taskName + " 禁用; ";
                    continue;
                }
                //执行停止任务的操作  只停止前节点
                JobUtils.stopJobTask(taskVo);
                if (Constant.LOCAL_HOSTNAME_IP.equals(taskVo.getRunHostNameIP())) {
                    taskVo.setRunHostNameIP("");
                }
                taskVo.setStatus(0);
                taskVo.setNextRunTime("无");
                success += taskName + " 成功; ";
                DataUtils.saveTaskInfo(taskVo);
            }
            String msg = "任务停止成功!\n其中：\n";
            if (GU.isNotNull(success)) {
                msg += "成功->" + success + "\n";
            }
            if (GU.isNotNull(fail)) {
                msg += "失败->" + fail;
            }
            return Feedback.success(msg);
        } catch (Exception e) {
            return Feedback.error("关闭任务报错:" +
                    e.getMessage());
        }
    }

    /**
     * 查看日志
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/viewRunLog")
    public Feedback viewRunLog(@RequestBody RequestDto requestDto) {
        try {
            //日志日期
            if (GU.isNull(requestDto.getLogDate())) {
                requestDto.setLogDate(DateUtils.getDateStr(DateUtils.YYYY_MM_DD));
            }
            //获取日志内容
            String logs = DataUtils.getLogs(requestDto);
            //日志文件日期集合
            List<String> logDateList = DataUtils.getLogDateList(requestDto);
            SimpleDateFormat format = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
            for (int i = 0; i < logDateList.size(); i++) {
                for (int j = 0; j < logDateList.size() -
                        i - 1; j++) {
                    long time1 = format.parse(logDateList
                            .get(j)).getTime();
                    long time2 = format.parse(logDateList
                            .get(j + 1)).getTime();
                    if (time1 <= time2) {
                        String temp = logDateList.get(j);
                        logDateList.set(j, logDateList.get(j + 1));
                        logDateList.set(j + 1, temp);
                    }
                }
            }
            //返回结果
            LogVo logVo = new LogVo();
            logVo.setLogs(logs);
            logVo.setLogDateList(logDateList);
            return Feedback.success(logVo);
        } catch (Exception e) {
            return Feedback.error("获取日志失败");
        }
    }

    @PostMapping(value = "/clearLogs")
    public Feedback clearLogs(@RequestBody RequestDto requestDto) {
        try {
            //清除日志
            DataUtils.clearLogs(requestDto);
            return Feedback.success("日志清理成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Feedback.error("日志清理失败");
        }
    }

    /**
     * 获取节点列表
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/getServers")
    public Feedback getServers(@RequestBody RequestDto requestDto) {
        try {
            //查询节点信息列表
            return Feedback.success(DataUtils.getServerInfoList());
        } catch (Exception e) {
            return Feedback.error("获取服务器列表失败");
        }
    }

    /**
     * 节点上线
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/serverAdd")
    public Feedback serverAdd(@RequestBody RequestDto requestDto) {
        try {
            //节点信息
            ServerInfo serverInfo = DataUtils.getServerInfo(requestDto.getRunHostNameIP());
            if (serverInfo.getEnable() == 1) {
                return Feedback.error(requestDto.getRunHostNameIP() + "该节点服务已启用，请勿重复启");
            }
            //设置节点可用
            serverInfo.setEnable(1);
            //保存节点信息
            DataUtils.saveServer(serverInfo);
            return Feedback.success(requestDto.getRunHostNameIP() + "节点服务器启用成功");
        } catch (Exception e) {
            return Feedback.error(requestDto.getRunHostNameIP() + "节点服务器启用失败");
        }
    }

    /**
     * 节点下线
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/serverDel")
    public Feedback serverDel(@RequestBody RequestDto requestDto) {
        try {
            //节点禁用
            ServerInfo serverInfo = DataUtils.getServerInfo(requestDto.getRunHostNameIP());
            if (serverInfo.getEnable() == 0) {
                return Feedback.error(requestDto.getRunHostNameIP() + "节点服务已停用，请勿重复停用"
                );
            }
            //社会自节点禁用
            serverInfo.setEnable(0);
            //保存节点信息
            DataUtils.saveServer(serverInfo);
            return Feedback.success(requestDto.getRunHostNameIP() + " 节点服务器停用成功");
        } catch (Exception e) {
            return Feedback.error(requestDto.getRunHostNameIP() + " 节点服务器停用失败");
        }
    }

    /**
     * 获取节点当前运行的任务信息
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/getRunningTask")
    public Feedback getRunningTask(@RequestBody RequestDto requestDto) {
        try {
            String runHostNameIP = requestDto.getRunHostNameIP();
            ServerInfo serverInfo = DataUtils.getServerInfo(runHostNameIP);
            List<TaskVo> taskVos = serverInfo.getTaskVos(
            );
            for (TaskVo taskVo : taskVos) {
                if (GU.isNull(taskVo.getRunHostNameIP())) {
                    taskVo.setRunHostNameIP("暂无");
                }
            }
            return Feedback.success(taskVos);
        } catch (Exception e) {
            return Feedback.error("获取该节点当前执行任务信息失败");
        }
    }

    /**
     * 获取参数值
     *
     * @return
     */
    @GetMapping(value = "/getPro")
    public Feedback getPro() {
        try {
            Constant.updateConfig();
            Properties properties = PropertityUtils.getProperties();
            return Feedback.success(properties);
        } catch (Exception e) {
            return Feedback.error("获取配置文件信失败");
        }
    }
}
