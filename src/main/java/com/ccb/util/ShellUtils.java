package com.ccb.util;

import com.ccb.common.CommonUtils;
import com.ccb.common.Constant;
import com.ccb.model.TaskVo;
import com.ccb.util.GU;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ShellUtils {

    //脚本执行线程池
    public static ThreadPoolExecutor runnable;
    static {
        runnable = new ThreadPoolExecutor(Constant.corePoolSize, Constant.maxPoolSize, 3, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.DiscardPolicy());
    }
    public static void executeShell(TaskVo taskVo, String
            hostNameIP) {
        //启动线程
        runnable.execute(new Runnable() {
            @Override
            public void run() {
                getShellRunVo(taskVo, hostNameIP);
            }
        });
    }
    /**
     * 执行脚本，获取执行结果
     */
    private static boolean getShellRunVo(TaskVo taskVo, String hostNameIP) {
        StringBuffer resultMsg = new StringBuffer();
        Process process = null;
        BufferedReader input = null;
        BufferedReader errorInput = null;
        //获取文件锁
        File shellLockFile = new File(Constant.shellPath
                + taskVo.getRunShellLockFileName());
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock lock = null;
        try {
            randomAccessFile = new RandomAccessFile(shellLockFile, "rw");
            fileChannel = randomAccessFile.getChannel();
            //尝试获取锁
            lock = fileChannel.tryLock();
            if (lock == null) {
                resultMsg.append(Constant.LOCAL_HOSTNAME_IP + " 尝试获取锁失败,或者有主机正在执行任务\n");
                return false;
            }
            resultMsg.append("操作主机：" + hostNameIP + "\n");
            resultMsg.append("当前执行主机：" + Constant.LOCAL_HOSTNAME_IP + "\n");
            //获取实时任务信息
            taskVo = DataUtils.getTaskInfoByTaskId(taskVo
                    .getTaskId());
            if (GU.isNull(taskVo) || taskVo.getStatus() == 0 || taskVo.getEnable() == 0) {
                JobUtils.stopJobTask(taskVo);
                resultMsg.append("执行脚本前的时，检测到任务已经停止/禁用，移除调度任务：" + taskVo.getTaskName() + "\n");
                return false;
            }
            //时间赋值
            taskVo.setLastRuntime(DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS));
            //执行节点
            taskVo.setRunHostNameIP(Constant.LOCAL_HOSTNAME_IP);
            DataUtils.saveTaskInfo(taskVo);
            resultMsg.append("时间：" + DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS) + " 任务名：" +
                            taskVo.getTaskName() + " 执行开始").append("\n");
            resultMsg.append("脚本内容：" + taskVo.getShellContent().trim()).append("\n");
            String shellPath = Constant.shellPath + taskVo.getRunShellFileName();
            resultMsg.append("脚本路径：" + shellPath).append("\n");
            File shellFile = new File(shellPath);
            if (!shellFile.exists()) {
                resultMsg.append("任务已删除或脚文件不存在\n").append("\n");
                return false;
            }
//            ServerInfo serverInfo = DataUtils.getServerInfo(Constant.LOCAL_HOSTNAME_IP);
//            if (serverInfo.getEnable() == 0) {
//                resultMsg.append(taskVo.getTaskName() +"执行任务过程中，检测到该节点服务器已禁用：" + Constant.LOCAL_HOSTNAME_IP);
//                return false;
//            }
            try {
                process = ShellSdk.doRun(shellPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (GU.isNull(process)) {
                resultMsg.append("时间：" + DateUtils.
                                getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS) + " 任务名" + taskVo.getTaskName() + " 执行异常");
                return false;
            }
            //读取执行结果返回的数据
            int resultCode = process.exitValue();
            resultMsg.append("执行结果返回码：" +
                    resultCode).append("\n");
//            if (taskVo.getIsNftPush() == 1) {
//                resultMsg.append("执行nft推送任务")
//                        .append("\n");
//                input = new BufferedReader(new InputStreamReader(process.getInputStream(), "ISO-8859-1"));
//            } else {
                input = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            }
            errorInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            resultMsg.append("执行结果正常日志：");
            while ((line = input.readLine()) != null) {
                resultMsg.append(line).append("\n");
            }
            resultMsg.append("时间：" + DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS) + " 任务名：" +
                            taskVo.getTaskName() + " 执行结束");
            lock.release();
            return true;
        } catch (Exception e) {
            resultMsg.append(Constant.LOCAL_HOSTNAME_IP +
                    "任务执行异常：任务名->" + taskVo.getTaskName(
            ) + e.getMessage()).append("\n");
            return false;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
            }
            try {
                if (errorInput != null) {
                    errorInput.close();
                }
            } catch (Exception e) {
            }
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
            }
            try {
                CommonUtils.releaseFileLock(lock, fileChannel, randomAccessFile);
            } catch (Exception e) {
            }
            try {
                taskVo.setRunHostNameIP("");
                if (taskVo.getOneTimeTask() == 1) {
                    taskVo.setStatus(0);
                    taskVo.setNextRunTime("无");
                } else {
                    TaskVo recent = DataUtils.getTaskInfoByTaskId(taskVo.getTaskId());
                    //获取任务的最新状态
                    taskVo.setStatus(recent.getStatus());
                }
                DataUtils.saveTaskInfo(taskVo);
                DataUtils.writelog(taskVo, resultMsg.toString());
            } catch (Exception e) {
            }
        }
    }
}
