package com.ccb.common;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CommonUtils {

    public static String readFile(String filePath) {
        InputStreamReader in = null;
        BufferedReader reader = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            in = new InputStreamReader(new FileInputStream(filePath),
                    "UTF-8");
            String result = "";
            String data = "";
            reader = new BufferedReader(in);
            while ((data = reader.readLine()) != null) {
                result += data + "\n";
            }
            return result;
        } catch (Exception e) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return "";
    }

    public static String readFile(File file) {
        InputStreamReader in = null;
        BufferedReader reader = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            in = new InputStreamReader(new FileInputStream(file), "UTF-8");
            String result = "";
            String data = "";
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(in);
            int lineCount = 0;
            //控制在5万行内，避免内存溢出
            while ((data = reader.readLine()) != null &&
                    lineCount < 50000) {
                lineCount++;
                sb.append(data);
                sb.append("\n");
            }
            result = sb.toString();
            return result;
        } catch (Exception e) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return "";
    }

    public static String readFileDecrypt(File file) {
        InputStreamReader in = null;
        BufferedReader reader = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            in = new InputStreamReader(new FileInputStream(file), "UTF-8");
            String result = "";
            String data = "";
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(in);
            int lineCount = 0;
            //控制在5万行内，避免内存溢出
            while ((data = reader.readLine()) != null &&
                    lineCount < 50000) {
                lineCount++;
                try {
                    data = "";
                    sb.append(data);
                    sb.append("\n");
                } catch (Exception e) {
                }
            }
            result = sb.toString();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return "";
    }

    public static void writeToFile(String filePath, String value) {
        OutputStreamWriter out = null;
        BufferedWriter fos = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8");
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new BufferedWriter(out);
            if (!StringUtils.isEmpty(value)) {
                //增加换行符，避免所有的记录写在同一行
                fos.write(value + "\n");
                fos.flush();
            }
        } catch (Exception e) {
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
        }
    }


    public static void writeToFile(String filePath, String value, boolean append) {
        OutputStreamWriter out = null;
        BufferedWriter fos = null;
        try {
            File file = new File(filePath);
            if (!new File(file.getParent()).exists()) {
                new File(file.getParent()).mkdirs();
            }
            out = new OutputStreamWriter(new FileOutputStream(file, append), "UTF-8");
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new BufferedWriter(out);
            if (!StringUtils.isEmpty(value)) {
                fos.write(value + "\r\n");
                fos.flush();
            }
        } catch (Exception e) {
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static void writeToFile(File file, String value, boolean append) {
        OutputStreamWriter out = null;
        BufferedWriter fos = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(file, append), "UTF-8");
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new BufferedWriter(out);
            if (!StringUtils.isEmpty(value)) {
                fos.write(value + "\r\n");
                fos.flush();
            }
        } catch (Exception e) {
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static void releaseFileLock(FileLock lock, FileChannel fileChannel, RandomAccessFile randomAccessFile) {
        try {
            if (null != lock) {
                lock.release();
            }
        } catch (Exception e) {
        }
        try {
            if (null != fileChannel) {
                fileChannel.close();
            }
        } catch (Exception e) {
        }
        try {
            if (null != randomAccessFile) {
                randomAccessFile.close();
            }
        } catch (Exception e) {
        }
    }


}

