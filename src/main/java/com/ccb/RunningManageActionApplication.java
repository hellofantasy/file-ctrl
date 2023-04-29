package com.ccb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.ccb" })
//@ServletComponentScan(value = "com.ccb.listener")
public class RunningManageActionApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunningManageActionApplication.class, args);
    }
}
