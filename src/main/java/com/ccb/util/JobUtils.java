package com.ccb.util;

import com.ccb.model.TaskVo;
import com.ccb.util.GU;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 调度任务工具类
 */
public class JobUtils {
    //组名
    public static String GROUP_NAME = "defaultGroup";
    //调度工厂
    public static SchedulerFactory schedulerFactory;
    //调度
    public static Scheduler scheduler;
    //触发器Map
    public static Map<String, Trigger> triggerMap = new HashMap<>();
    static {
        try {
            schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
        }
    }
    /**
     * 启动任务
     *
     * @param taskVo
     * @return
     */
    public static boolean startJobTask(TaskVo taskVo) {
        try {
            //触发器Map 有这个任务ID 说明已经            启动过了，不要再重复启动
            if (triggerMap.containsKey(taskVo.getTaskId()
            )) {
                return true;
            }
            //往执行任务的job传入参数
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("taskVo", taskVo);
            //创建任务执行类
            JobDetail jobDetail = JobBuilder.newJob(TaskDetail.class).usingJobData(jobDataMap).withIdentity(taskVo
                    .getTaskId(), GROUP_NAME).build();
            //创建触发器
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(taskVo.getTaskId(), GROUP_NAME).forJob(jobDetail).withSchedule(CronScheduleBuilder.cronSchedule(taskVo.getCronExpression())).build();
            //加入调度列表
            scheduler.scheduleJob(jobDetail, trigger);
            //存储触发器
            triggerMap.put(taskVo.getTaskId(), trigger);

            return true;
        } catch (SchedulerException e) {
            return false;
        }
    }
    /**
     * 关闭任务
     *
     * @param taskVo
     * @return
     */
    public static  boolean stopJobTask(TaskVo taskVo) {
        try {
            //是否启动过该任务
            if (triggerMap.containsKey(taskVo.getTaskId()
            )) {
                //获取触发器
                Trigger trigger = triggerMap.get(taskVo.getTaskId());
                //根据触发器的key移除调度
                boolean result = scheduler.unscheduleJob(
                        trigger.getKey());
                //移除触发器
                triggerMap.remove(taskVo.getTaskId());
                return result;
            }
            return false;
        } catch (Exception e) {
        }
        return false;
    }
    /**
     * 获取任务的下一次执行时间
     *
     * @param taskVo
     * @return
     */
    public static String getTaskNextRunTime(TaskVo taskVo
    ) {
        try {
            if (GU.isNull(taskVo.getCronExpression())) {
                return "";
            }
            CronExpression cronExpression = new CronExpression(taskVo.getCronExpression());
            String date2Str = DateUtils.formatDate2Str(cronExpression.getNextValidTimeAfter(new Date()));
            return date2Str;
        } catch (ParseException e) {
        }
        return "";
    }
}