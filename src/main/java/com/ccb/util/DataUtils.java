package com.ccb.util;

import com.alibaba.fastjson.JSON;
import com.ccb.common.CommonUtils;
import com.ccb.common.Constant;
import com.ccb.model.RequestDto;
import com.ccb.model.ServerInfo;
import com.ccb.model.TaskVo;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataUtils {

    //文件路径初始化
    static {
        if (!new File(Constant.taskInfosCacheFilePath).exists()) {
            new File(Constant.taskInfosCacheFilePath).mkdirs();
        }
        if (!new File(Constant.shellPath).exists()) {
            new File(Constant.shellPath).mkdirs();
        }
        if (!new File(Constant.taskFilePath).exists()) {
            new File(Constant.taskFilePath).mkdirs();
        }
        if (!new File(Constant.logsPath).exists()) {
            new File(Constant.logsPath).mkdirs();
        }
        if (!new File(Constant.serversPath).exists()) {
            new File(Constant.serversPath).mkdirs();
        }
    }
    /**
     * 读取任务信息列表
     * 读取以task-开头的文件信息
     *
     * @return
     */
    public static List<TaskVo> getTaskVoList() {
        List<TaskVo> taskInfos = new ArrayList<>();
        try {
            //任务信息路径
            File taskInfoFiles = new File(Constant.taskFilePath);
            //遍历路径下的所有未文件
            //File[] files = taskInfoFiles.listFiles();
            File[] files = new File(taskInfoFiles.getAbsolutePath()).listFiles();
            for (int i = 0; i < files.length; i++) {
                //任务文件以 task-开头
                if (!files[i].getName().startsWith("task-")) {
                continue;
            }
            //度读取任务文件信息
            String taskStr = CommonUtils.readFile(files[i]);
            if (GU.isNull(taskStr)) {
                continue;
            }
            //json 转java实体对象
            try {
                //TaskVo taskVo = JSON.toJavaObject(JSON.parseObject(taskStr), TaskVo.class);
                TaskVo taskVo = JSON.parseObject(taskStr, TaskVo.class);
                //状态值翻译
                getCodeValue(taskVo);
                taskInfos.add(taskVo);
            } catch (Exception e) {
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
        return taskInfos;
}
    /**
     * 读取任务信息列表
     * 根据任务ID获取任务信息
     *
     * @return
     */
    public static TaskVo getTaskInfoByTaskId(String taskId) {
        //任务信息文件
        File file = new File(Constant.taskFilePath + "task-" + taskId + ".task");
        if (!file.exists()) {
            return null;
        }
        //读去任务信息
        String taskStr = CommonUtils.readFile(file);
        if (GU.isNull(taskStr)) {
            return null;
        }
        //json转java实体
        TaskVo taskVo = JSON.toJavaObject(JSON.parseObject(taskStr), TaskVo.class);
        //状态值翻译
        getCodeValue(taskVo);
        return taskVo;
    }
    //状态值翻译
    private static void getCodeValue(TaskVo taskVo) {
        taskVo.setEnableMsg(taskVo.getEnable() == 1 ? "启用" : "禁用");
                taskVo.setStatusMsg(taskVo.getStatus() == 1 ? "运行中" : "准备就绪");
                        taskVo.setOneTimeTaskMsg(taskVo.getOneTimeTask()
                                == 1 ? "是" : "否");
        taskVo.setIsNftPushMsg(taskVo.getIsNftPush() == 1
                ? "是" : "否");
    }
    /**
     * 读取任务信息Map
     *
     * @return
     */
    public static Map<String, TaskVo> getTaskVoMap() {
        Map<String, TaskVo> taskVoMap = new HashMap<>();
        File taskInfoFiles = new File(Constant.taskFilePath);
        //File[] files = taskInfoFiles.listFiles();
        File[] files = new File(taskInfoFiles.getAbsolutePath()).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].getName().startsWith("task-"))
            {
                continue;
            }
            String taskStr = CommonUtils.readFile(files[i
                    ]);
            if (GU.isNull(taskStr)) {
                continue;
            }
            TaskVo taskVo = JSON.toJavaObject(JSON.parseObject(taskStr), TaskVo.class);
            //状态值翻译
            getCodeValue(taskVo);
            taskVoMap.put(taskVo.getTaskId(), taskVo);
        }
        return taskVoMap;
    }
    /**
     * @param taskVo 保存的实体对象
     */
    public static boolean addTaskInfo(TaskVo taskVo) {
        //任务信息文件锁
        File lockFile = new File(Constant.taskFilePath +
                taskVo.getTaskInfoLockFileName());
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock lock = null;
        try {
            //不存在则创建
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }
            randomAccessFile = new RandomAccessFile(lockFile, "rw");
            fileChannel = randomAccessFile.getChannel();
            //尝试获取锁
            lock = fileChannel.tryLock();
            if (lock == null) {
                return false;
            }
            String taskFile = Constant.taskFilePath + taskVo.getTaskInfoFileName();
            //把脚本文件写入文件
            DataUtils.writeShell(taskVo);
            //保存任务信息
            CommonUtils.writeToFile(taskFile, taskVo.toString());
            lock.release();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            //释放锁
            CommonUtils.releaseFileLock(lock, fileChannel
                    , randomAccessFile);
        }
    }
    /**
     * @param taskVo 保存的实体对象
     */
    public static boolean saveTaskInfo(TaskVo taskVo) {
        //任务信息文件锁
        File lockFile = new File(Constant.taskFilePath +
                taskVo.getTaskInfoLockFileName());
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock lock = null;
        try {
            //不存在则创建
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }
            randomAccessFile = new RandomAccessFile(lockFile, "rw");
            fileChannel = randomAccessFile.getChannel();
            //尝试获取锁
            try {
                lock = fileChannel.tryLock();
            } catch (Exception e) {
                //获取锁失败
                return false;
            }
            if (lock == null) {
                return false;
            }
            String taskFile = Constant.taskFilePath + taskVo.getTaskInfoFileName();
            CommonUtils.writeToFile(taskFile, taskVo.toString());
            //释放锁
            lock.release();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            //释放锁
            CommonUtils.releaseFileLock(lock, fileChannel
                    , randomAccessFile);
        }
    }
    /**
     * @param taskVo 要删除的实体对象
     */
    public static boolean deleteTaskInfo(TaskVo taskVo) {
        File lockFile = new File(Constant.taskFilePath +
                taskVo.getTaskInfoLockFileName());
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock lock = null;
        try {
            randomAccessFile = new RandomAccessFile(lockFile, "rw");
            fileChannel = randomAccessFile.getChannel();
            lock = fileChannel.tryLock();
            if (lock == null) {
                return false;
            }
            File taskFile = new File(Constant.taskFilePath + taskVo.getTaskInfoFileName());
            if (taskFile.exists()) {
                taskFile.delete();
            }
            //删除运行的脚本
            String taskShellPath = Constant.shellPath + taskVo.getRunShellFileName();
            File file = new File(taskShellPath);
            if (file.exists()) {
                file.delete();
            }
            JobUtils.stopJobTask(taskVo);
            lock.release();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            CommonUtils.releaseFileLock(lock, fileChannel
                    , randomAccessFile);
        }
    }
    /**
     * 把脚本内容写入文件
     *
     * @param taskVo
     * @return
     */
    public static String writeShell(TaskVo taskVo) {
        try {
            File shellLockFile = new File(Constant.shellPath + taskVo.getRunShellLockFileName());
            if (!shellLockFile.exists()) {
                shellLockFile.createNewFile();
            }
            String taskShellPath = Constant.shellPath + taskVo.getRunShellFileName();
            CommonUtils.writeToFile(taskShellPath, taskVo
                    .getShellContent());
            if (!File.separator.contains("\\")) {
                ShellSdk.changeFilePermission(taskShellPath);
            }
//            new ProcessBuilder("/bin/chmod", "777", tas            kShellPath).start().waitFor(10, TimeUnit.SECONDS);
            return taskShellPath;
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 写日志
     *
     * @param taskVo
     * @param runShellLog
     */
    public static void writelog(TaskVo taskVo, String runShellLog) {
        try {
            File logFile = new File(Constant.logsPath + "taskRunLog-" + taskVo.getTaskId() + "-" + DateUtils.getDateStr(DateUtils.YYYY_MM_DD) + ".log");
            String loginfo = runShellLog + "\n-------------------------------------------------------------------- --------------------------------------------";
            CommonUtils.writeToFile(logFile, loginfo, true);
        } catch (Exception e) {
        }
    }
    /**
     * 获取日志
     *
     * @param requestDto
     * @return
     */
    public static String getLogs(RequestDto requestDto) {
        File logFilePath = new File(Constant.logsPath + "taskRunLog-" + requestDto.getTaskId() + "-" + requestDto.
                getLogDate() + ".log");
        //String logStr = CommonUtils.readFile(logFilePat
        String logStr = NcdmFileUtils.fileTail(logFilePath.getAbsolutePath(), 2000);
        return logStr;
    }
    /**
     * 获取日志日期列表
     *
     * @param requestDto
     * @return
     */
    public static List<String> getLogDateList(RequestDto
                                                      requestDto) {
        File file = new File(Constant.logsPath);
        //File[] logFiles = file.listFiles();
        File[] logFiles = NcdmFileUtils.listDirFiles(file
                .getAbsolutePath(), 10000, 10000, null);
        List<String> logDateList = new ArrayList<>();
        for (int i = 0; i < logFiles.length; i++) {
            if (!logFiles[i].getName().startsWith("taskRunLog-" + requestDto.getTaskId())) {
            continue;
        }
        String logDate = logFiles[i].getName().replace("taskRunLog-" + requestDto.getTaskId() + "-", "").replace(".log", "");
        logDateList.add(logDate);
    }
        return logDateList;
                }
/**
 * 清除日志
 *
 * @param requestDto
 */
public static void clearLogs(RequestDto requestDto) {
        try {
        File file = new File(Constant.logsPath);
        if (GU.isNotNull(requestDto.getLogDate())) {
        file = new File(Constant.logsPath + "taskRunLog-" + requestDto.getTaskId() + "-" + requestDto.getLogDate() + ".log");
        if (file.exists()) {
        file.delete();
        }
        } else {
        //File[] logFiles = file.listFiles();
        File[] logFiles = NcdmFileUtils.listDirFiles(file.getAbsolutePath(), 10000, 10000, null);
        for (int i = 0; i < logFiles.length; i++)
        {
        if (!logFiles[i].getName().startsWith
        ("taskRunLog-" + requestDto.getTaskId())) {
        continue;
        }
        logFiles[i].delete();
        }
        }
        } catch (Exception e) {
        }
        }
/**
 * 节点服务上线
 */
public static void serverLogin() {
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock lock = null;
        try {
        ServerInfo serverInfo = new ServerInfo();
        File serverFile = new File(Constant.serversPath + Constant.LOCAL_HOSTNAME_IP + ".txt");
        File serverLockFile = new File(Constant.serversPath + Constant.LOCAL_HOSTNAME_IP + ".LOCK");
        if (!serverLockFile.exists()) {
        serverLockFile.createNewFile();
        }
        randomAccessFile = new RandomAccessFile(serverLockFile, "rw");
        fileChannel = randomAccessFile.getChannel();
        lock = fileChannel.tryLock();
        if (lock == null) {
        return;
        }
        if (!serverFile.exists()) {
        serverFile.createNewFile();
        serverInfo.setRunHostNameIP(Constant.LOCAL_HOSTNAME_IP);
        serverInfo.setServerId(System.currentTimeMillis() + "");
        serverInfo.setEnable(0);
        } else {
        String serverStr = CommonUtils.readFile(serverFile);
        serverInfo = JSON.toJavaObject(JSON.parseObject(serverStr), ServerInfo.class);
        }
        serverInfo.setRunStatus(1);
        serverInfo.setTaskVos(new ArrayList<>());
        serverInfo.setDeployTime(DateUtils.getDateStr
        (DateUtils.YYYY_MM_DD_HH_MM_SS));
        CommonUtils.writeToFile(serverFile, serverInfo.toString(), false);
        lock.release();
        } catch (Exception e) {
        } finally {
        CommonUtils.releaseFileLock(lock, fileChannel
        , randomAccessFile);
        }
        }
/**
 * 节点服务下线
 */
public static void serverLogout() {
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock lock = null;
        try {
        File serverFile = new File(Constant.serversPath + Constant.LOCAL_HOSTNAME_IP + ".txt");
        File serverLockFile = new File(Constant.serversPath + Constant.LOCAL_HOSTNAME_IP + ".LOCK");
        randomAccessFile = new RandomAccessFile(serverLockFile, "rw");
        fileChannel = randomAccessFile.getChannel();
        lock = fileChannel.tryLock();
        if (lock == null) {
        return;
        }
        String serverStr = CommonUtils.readFile(serverFile);
        ServerInfo serverInfo = JSON.toJavaObject(JSON.parseObject(serverStr), ServerInfo.class);
        serverInfo.setRunStatus(0);
        serverInfo.setEnable(0);
        serverInfo.setTaskVos(new ArrayList<>());
        serverInfo.setDeployTime("");
        CommonUtils.writeToFile(serverFile, serverInfo.toString(), false);
        lock.release();
        } catch (Exception e) {
        } finally {
        CommonUtils.releaseFileLock(lock, fileChannel
        , randomAccessFile);
        }
        }
/**
 * 获取节点服务信息列表
 *
 * @return
 */
public static List<ServerInfo> getServerInfoList() {
        List<ServerInfo> serverInfos = new ArrayList<>();
        try {
        File severInfoFile = new File(Constant.serversPath);
        //File[] files = severInfoFile.listFiles();
        File[] files = NcdmFileUtils.listDirFiles(severInfoFile.getAbsolutePath(), 10000, 10000, null);
        for (int i = 0; i < files.length; i++) {
        String serverStr = CommonUtils.readFile(files[i]);
        if (GU.isNull(serverStr)) {
        continue;
        }
        ServerInfo serverInfo = JSON.toJavaObject
        (JSON.parseObject(serverStr), ServerInfo.class);
        //状态值翻译
        serverInfo.setEnableMsg(serverInfo.getEnable() == 1 ? "启用" : "停用");
        serverInfo.setRunStatusMsg(serverInfo.getRunStatus() == 1 ? "上线" : "下线");
        serverInfos.add(serverInfo);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
        for (int i = 0; i < serverInfos.size() - 1; i
        ++) {
        for (int j = 0; j < serverInfos.size() -
        i - 1; j++) {
        String deployTime1 = serverInfos.get(
        j).getDeployTime();
        String deployTime2 = serverInfos.get(
        j + 1).getDeployTime();
        if (GU.isNull(deployTime1)) {
        deployTime1 = "1970-01-01 00:00:00";
        }
        if (GU.isNull(deployTime2)) {
        deployTime2 = "1970-01-01 00:00:00";
        }
        long time1 = simpleDateFormat.parse(deployTime1).getTime();
        long time2 = simpleDateFormat.parse(deployTime2).getTime();
        if (time1 <= time2) {
        ServerInfo temp = serverInfos.get
        (j);
        serverInfos.set(j, serverInfos.get(j + 1));
        serverInfos.set(j + 1, temp);
        }
        }
        }
        } catch (ParseException e) {
        }
        return serverInfos;
        }
/**
 * 获取节点信息
 *
 * @param runHostNameIP
 * @return
 */
public static ServerInfo getServerInfo(String runHostNameIP) {
        try {
        File serverFile = new File(Constant.serversPath + runHostNameIP + ".txt");
        String serverStr = CommonUtils.readFile(serverFile);
        ServerInfo serverInfo = JSON.toJavaObject(JSON.parseObject(serverStr), ServerInfo.class);
        return serverInfo;
        } catch (Exception e) {
        return new ServerInfo();
        }
        }
/**
 * 保存节点服务信息
 *
 * @param serverInfo
 */
public static void saveServer(ServerInfo serverInfo)
        {
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock lock = null;
        try {
        File serverFile = new File(Constant.serversPath + serverInfo.getRunHostNameIP() + ".txt");
        File serverLockFile = new File(Constant.serversPath + Constant.LOCAL_HOSTNAME_IP + ".LOCK");
        randomAccessFile = new RandomAccessFile(serverLockFile, "rw");
        fileChannel = randomAccessFile.getChannel();
        lock = fileChannel.tryLock();
        if (lock == null) {
        return;
        }
        CommonUtils.writeToFile(serverFile, serverInfo.toString(), false);
        lock.release();
        } catch (Exception e) {
        } finally {
        CommonUtils.releaseFileLock(lock, fileChannel
        , randomAccessFile);
        }
        }

}
