package com.ccb.util;

import com.ccb.common.CommonUtils;
import com.ccb.common.Constant;
import com.ccb.model.ServerInfo;
import com.ccb.model.ShellRunVo;
import com.ccb.model.TaskVo;
import com.ccb.util.GU;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class TaskDetail implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        ShellRunVo shellRunVo = new ShellRunVo();
        TaskVo tv1 = (TaskVo) jobExecutionContext.getJobDetail().getJobDataMap().get("taskVo");
        TaskVo taskVo = null;
        if (tv1 != null) {
            //获取实时的任务信息
            taskVo = DataUtils.getTaskInfoByTaskId(tv1.getTaskId());

            if (GU.isNull(taskVo) || taskVo.getStatus() ==
                    0) {
                shellRunVo.setMsg("执行任务的时候检测到任务已经停止，移除调度任务：" + tv1.getTaskName());
                JobUtils.stopJobTask(tv1);
                return;
            }
        }else {
            return;
        }

        //获取节点信息
        ServerInfo serverInfo = DataUtils.getServerInfo(Constant.LOCAL_HOSTNAME_IP);
        //节点禁用
        if (serverInfo.getEnable() == 0) {
            shellRunVo.setMsg(taskVo.getTaskName() + "执行任务过程中，检测到该节点服务器已被禁用：" + Constant.LOCAL_HOSTNAME_IP);
                    JobUtils.stopJobTask(taskVo);
            return;
        }
        //非一次性任务
        if (taskVo.getOneTimeTask() != 1) {
            taskVo.setNextRunTime(JobUtils.getTaskNextRunTime(taskVo));
        } else {
            taskVo.setNextRunTime("无");
        }
        DataUtils.saveTaskInfo(taskVo);
        //执行任务信息的文件锁
        File lockFile = new File(Constant.taskFilePath +
                taskVo.getTaskInfoLockFileName());
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock lock = null;
        try {
            randomAccessFile = new RandomAccessFile(lockFile, "rw");
            fileChannel = randomAccessFile.getChannel();
            //尝试获取锁
            lock = fileChannel.tryLock();
            if (lock == null) {
                shellRunVo.setMsg(Constant.LOCAL_HOSTNAME_IP + " 尝试获取锁失败:" + lockFile.getName());
                return;
            }
            //执行脚本
            long startTime = System.currentTimeMillis();
            ShellUtils.executeShell(taskVo,"");
            long endTime = System.currentTimeMillis();
            if (endTime - startTime < 30000) {
                try {
                    Thread.sleep(30000 - (endTime - startTime))
                    ;
                }catch(Exception e) {
                }
            }
            //释放文件锁
            lock.release();
        } catch (Exception e) {
        } finally {
            //释放文件锁
            CommonUtils.releaseFileLock(lock, fileChannel
                    , randomAccessFile);

            try {
                DataUtils.writelog(taskVo, shellRunVo.getMsg
                        () + "\n");
            }catch(Exception e) {
            }


        }
    }
}