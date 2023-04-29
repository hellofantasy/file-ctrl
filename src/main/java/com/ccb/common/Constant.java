package com.ccb.common;

import com.ccb.util.PropertityUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Constant {
    //当前主机_IP
    public static final String LOCAL_HOSTNAME_IP = getHostNameIP();
    //用户主路径
    public static final String USER_HOME;
    //任务调度程序配置路径
    public static String taskInfosCacheFilePath;
    //核心线程数
    public static Integer corePoolSize;
    //最大线程数
    public static Integer maxPoolSize;
    //日志清理定时Cron表达式
    public static String logClearCron;
    //任务扫描定时Cron表达式
    public static String taskQueryCron;
    //任务信息存储路径
    public static String taskFilePath;
    //shell文件存储路径
    public static String shellPath;
    //日志存储路径
    public static String logsPath;
    //节点服务信息存储路径
    public static String serversPath;
    public static long logCacheDayCount;
    public static String fileSuffixs;
    public static String version;

    static {
        //windows
        if (File.separator.contains("\\")) {
            USER_HOME = "D:/home/ap/ccda/";
            if (!new File(USER_HOME).exists()) {
                new File(USER_HOME).mkdirs();
            }
        } else {
            //linux unix
            USER_HOME = System.getProperty("user.home") + File.separator;
        }
        updateConfig();
    }

    /**
     * 更新配置文件
     */
    public static void updateConfig() {
        Properties properties = PropertityUtils.getProperties();
        taskInfosCacheFilePath = properties.getProperty("taskInfosCacheFilePath", USER_HOME + "ccda_config/batch-schedule/");
        if (!new File(taskInfosCacheFilePath).exists()) {
            new File(taskInfosCacheFilePath).mkdirs();
        }
        corePoolSize = Integer.parseInt(properties.getProperty("corePoolSize", "4"));
        maxPoolSize = Integer.parseInt(properties.getProperty("maxPoolSize", "4"));
        logClearCron = properties.getProperty("logClearCron", "0 0 0 */1 * ?");
        taskQueryCron = properties.getProperty("taskQueryCron", "0 */1 * * * ?");
        version = properties.getProperty("version", " V1.2");
        fileSuffixs = properties.getProperty("fileSuffixs", ".jar,.war,.gz,.tar,.zip,.rar,.doc,.docx,.pptx,.ppt,.xls,.xlsx,.original,.ser,.avi,.mp3,.mp4,.xz,.jpg,.png,.gif,.pdf,.class");
        shellPath = taskInfosCacheFilePath + "shells" + File.separator;
        taskFilePath = taskInfosCacheFilePath + "taskInfos" + File.separator;
        logsPath = taskInfosCacheFilePath + "logs" + File.separator;
        serversPath = taskInfosCacheFilePath + "servers" + File.separator;
        logCacheDayCount = Long.parseLong(properties.getProperty("logCacheDayCount", "30"));
    }

    /**
     * 获取当前主机名_IP
     *
     * @return
     */
    public static String getHostNameIP() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
        }
        if (addr != null) {
            return addr.getHostName() + "_" + addr.getHostAddress();
        }
        return null;
    }

}
