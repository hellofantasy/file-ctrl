package com.ccb.util;

import com.ccb.common.Constant;
import com.ccb.util.GU;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 解析 Properties 配置文件
 * key=value
 */
public class PropertityUtils {
    //
    private static String pcConfigPath = Constant.USER_HOME + "batch-schedule-config.properties";
    //配置文件上次修改时间
    private static Long CONFIG_FILE_LAST_MODIFY_TIME = new File(pcConfigPath).lastModified();
    private static Properties pcConfigProperties = new Properties();
    public static Properties getProperties() {

        InputStream is = null;
        try {
            //先读取配置文件，看是否修改过
            File file = new File(pcConfigPath);
            if (!file.exists()) {
                file.createNewFile();
                pcConfigProperties = new Properties();
                return pcConfigProperties;
            }
            if (file.lastModified() > CONFIG_FILE_LAST_MODIFY_TIME || GU.isNull(pcConfigProperties)) {
                Properties properties = new Properties();
                is = new FileInputStream(pcConfigPath);
                properties.load(is);
                pcConfigProperties = properties;
                CONFIG_FILE_LAST_MODIFY_TIME = file.lastModified();
            }
            return pcConfigProperties;
        } catch (Exception e) {
            throw new RuntimeException("batch-schedule-config.properties read error!", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
    }
}