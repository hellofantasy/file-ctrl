package com.ccb.util;

import java.util.concurrent.TimeUnit;

public class ShellSdk {

    public static Process doRun(String shellPath){

        ProcessBuilder processBuilder=new ProcessBuilder(new String[]{shellPath});
        try {
            Process process=processBuilder.start();
            process.waitFor(30,TimeUnit.SECONDS);
            return process;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void changeFilePermission(String path) {
        try {
            new ProcessBuilder("/bin/chmod", "777", path).start().waitFor(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
