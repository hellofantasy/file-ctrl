package com.ccb.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ReadThread extends Thread {
    Semaphore sig;
    Process process;
    boolean bErrorRead = false;
    long timeOut;
    int maxLines;
    long startTime;
    boolean bWindows = false;
    private List<String> listString = null;
    public List<String> getListString() {
        return this.listString;
    }
    public void setListString(List<String> listString) {
        this.listString = listString;
    }
    public ReadThread(Semaphore sig, Process p, boolean bError, long timeOut, int maxLines) {
        this.sig = sig;
        this.process = p;
        this.bErrorRead = bError;
        this.timeOut = timeOut;
        this.maxLines = maxLines;
        this.startTime = System.currentTimeMillis();
    }
    public void setIsWindwows(boolean isWindows) {
        this.bWindows = isWindows;
    }
    public void run() {
        InputStream ins = null;
        InputStreamReader insReader = null;
        BufferedReader reader = null;
        try {
            if (this.bErrorRead) {
                ins = this.process.getErrorStream();
                insReader = new InputStreamReader(ins, this.bWindows ? "GBK" : "UTF-8");
                reader = new BufferedReader(insReader);
            } else {
                ins = this.process.getInputStream();
                insReader = new InputStreamReader(ins, this.bWindows ? "GBK" : "UTF-8");
                reader = new BufferedReader(insReader);
            }
            String line = null;
            int count = 0;
            this.listString = new ArrayList();
            long curTime;
            do {
                if ((line = reader.readLine()) == null) {
                    return;
                }
                ++count;
                this.listString.add(line);
                curTime = System.currentTimeMillis();
            } while(count < this.maxLines && curTime <= this.startTime + this.timeOut);
        } catch (Exception var25) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException var24) {
                }
            }
            if (insReader != null) {
                try {
                    insReader.close();
                } catch (IOException var23) {
                }
            }
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException var22) {
                }
            }
            this.sig.release();
        }
    }
}
