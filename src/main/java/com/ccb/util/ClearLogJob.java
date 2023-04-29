package com.ccb.util;

import com.ccb.common.Constant;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;

public class ClearLogJob  implements Job {
    //日志主路径
    private static String logsPath = Constant.taskInfosCacheFilePath + "logs" + File.separator;
    //
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        long expireTimeMills = Constant.logCacheDayCount
                * 24 * 60 * 60 * 1000;
        File logsFile = new File(logsPath);
        //File[] logFiles = logsFile.listFiles();
        File[] logFiles = NcdmFileUtils.listDirFiles(logsFile.getAbsolutePath(), 10000, 10000, null);
        for (int i = 0; i < logFiles.length; i++) {
            if (System.currentTimeMillis() - logFiles[i].
                    lastModified() > expireTimeMills) {
                logFiles[i].delete();
            }
        }
    }
}