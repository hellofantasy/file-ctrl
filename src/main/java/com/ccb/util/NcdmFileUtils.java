package com.ccb.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class NcdmFileUtils {

    public static File[] listDirFiles(String absoluteDir,
                                      int maxLine, long timeOut, String mtime) {
        ArrayList<String> listStr = listDirFilesStrs(absoluteDir, maxLine, timeOut, mtime);
        List<File> fileList = new ArrayList();
        if (listStr != null) {
            Iterator var8 = listStr.iterator();
            while(var8.hasNext()) {
                String itm = (String)var8.next();
                if (!itm.equals(absoluteDir)) {
                    fileList.add(new File(itm));
                }
            }
        }
        return (File[])fileList.toArray(new File[0]);
    }
    public static String fileTail(String filePath, int maxLine) {
        String str = null;
        try {
            str = tailOfFile(filePath, maxLine, 10000L);
        } catch (Exception var4) {
        }
        return str;
    }
    private static String tailOfFile(String filePath, int
            maxLine, long timeOut) {
        new ArrayList();
        RandomAccessFile f = null;
        String result = null;
        try {
            f = new RandomAccessFile(filePath, "r");
            long length = f.length();
            long lineCount = 0L;
            byte[] bs = new byte[1048576];
            long curPos = 0L;
            int index = 1;
            int readLen;
            for(boolean var15 = false; lineCount < (long)
                    maxLine; ++index) {
                curPos = length - (long)(index * 1048576)
                ;
                if (curPos < 0L) {
                    curPos = 0L;
                    break;
                }
                f.seek(curPos);
                readLen = f.read(bs, 0, bs.length);
                boolean needBreak = false;
                for(int i = readLen - 1; i >= 0; --i) {
                    if (bs[i] == 10) {
                        ++lineCount;
                        if (lineCount >= (long)maxLine) {
                            curPos += (long)i;
                            needBreak = true;
                            break;
                        }
                    }
                }
                if (needBreak) {
                    break;
                }
            }
            f.seek(curPos);
            int size = (int)(length - curPos);
            readLen = f.read(bs, 0, bs.length);
            ByteBuffer bbs;
            for(bbs = ByteBuffer.allocate(size); readLen
                    > 0; readLen = f.read(bs, 0, bs.length)) {
                bbs.put(bs, 0, readLen);
            }
            result = new String(bbs.array(), 0, bbs.position(), "UTF-8");
        } catch (FileNotFoundException var28) {
        } catch (Exception var29) {
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException var27) {
                }
            }
        }
        return result;
    }
    public static ArrayList<String> listDirFilesStrs(String absoluteDir, int maxLine, long timeOut, String mtime)
    {
        ProcessBuilder process = null;
        boolean bWindows = false;
        if (File.separator.equals("\\")) {
            process = new ProcessBuilder(new String[]{"cmd.exe", "/C", "dir " + absoluteDir + " /B"});
                    bWindows = true;
        } else if (mtime == null) {
                process = new ProcessBuilder(new String[]{"find", absoluteDir, "-maxdepth", "1"});
                } else {
                    process = new ProcessBuilder(new String[]{"find", absoluteDir, "-maxdepth", "1", "-mtime", mtime});
                    }
                            ArrayList fileList = null;
                    try {
                        Semaphore sig = new Semaphore(2);
                        Process p = process.start();
                        ReadThread stdThread = new ReadThread(sig, p,
                                false, timeOut, maxLine);
                        ReadThread errThread = new ReadThread(sig, p,
                                true, timeOut, maxLine);
                        try {
                            sig.acquire(2);
                        } catch (InterruptedException var14) {
                        }
                        if (bWindows) {
                            stdThread.setIsWindwows(true);
                            errThread.setIsWindwows(true);
                        }
                        stdThread.start();
                        errThread.start();
                        try {
                            sig.tryAcquire(2, timeOut, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException var13) {
                        }
                        List<String> listStr = stdThread.getListString();
                        if (bWindows) {
                            preProcessWindows(listStr, absoluteDir);
                        }
                        fileList = (ArrayList)listStr;
                    } catch (Exception var15) {
                        fileList = new ArrayList();
                    }
                    return fileList;
                }
                private static List<String> preProcessWindows(List<String> listStr, String absoluteDir) {
                    String sp = File.separator;
                    boolean needAddSp = false;
                    if (absoluteDir.endsWith(sp)) {
                        needAddSp = false;
                    } else {
                        needAddSp = true;
                    }
                    if (listStr != null) {
                        int size = listStr.size();
                        for(int i = 0; i < size; ++i) {
                            String newItem = (String)listStr.get(i);
                            if (needAddSp) {
                                newItem = absoluteDir + sp + newItem;
                            } else {
                                newItem = absoluteDir + newItem;
                            }
                            listStr.set(i, newItem);
                        }
                    }
                    return listStr;
                }
                public static List<String> listCharset() {
                    SortedMap<String, Charset> s = Charset.availableCharsets();
                    Set<String> keys = s.keySet();
                    List<String> list = new ArrayList();
                    Iterator var4 = keys.iterator();
                    while(var4.hasNext()) {
                        String k = (String)var4.next();
                        list.add(k);
                    }
                    return list;
                }
            }
