package com.ccb.util;

import com.ccb.util.GU;

import java.io.*;
import java.util.zip.*;

public class ZipUtils {
    public static void toZip(String srcDir, OutputStream
            out, boolean keepDirStructure) {
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(out);
            File file = new File(srcDir);
            compress(file, zipOutputStream, file.getName(
            ), keepDirStructure);
        } catch (Exception e) {
        } finally {
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void compress(File sourceFile, ZipOutputStream zipOutputStream, String name, boolean keepDirsStructure) throws IOException {
        byte[] buf = new byte[1024 * 2];
        if (sourceFile.isFile()) {
            zipOutputStream.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            while ((len = fileInputStream.read(buf)) != -
                    1) {
                zipOutputStream.write(buf, 0, len);
                zipOutputStream.flush();
            }
            zipOutputStream.closeEntry();
            fileInputStream.close();
        } else {
            File[] listFile = NcdmFileUtils.listDirFiles(
                    sourceFile.getAbsolutePath(),
                    100000, 100000, null);
            if (GU.isNotNull(listFile)) {
                for (int i = 0; i < listFile.length; i++)
                {
                    if (keepDirsStructure) {
                        compress(listFile[i], zipOutputStream, name + "/" +
                                        listFile[i].getName(), keepDirsStructure);
                    } else {
                        compress(listFile[i], zipOutputStream, listFile[i].getName(),
                                keepDirsStructure);
                    }
                }
            }
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
//        FileOutputStream fileOutputStream = new FileOutputStream("H:\\test.zip");
//        toZip("H:\\home\\ap\\ccda\\ser\\", fileOutputStream, true);
        System.out.println(System.getProperty("user.home"
        ));
    }
}
