package com.ccb.util;

import com.ccb.common.Constant;
import com.ccb.model.FileVo;
import com.ccb.util.GU;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileUtils {

    public static String[] suffixs = Constant.fileSuffixs
            .toLowerCase().split(",");
    public static boolean fileEditable(String fileName) {
        for (String obj : suffixs) {
            if (fileName.toLowerCase().endsWith(obj)) {
                return false;
            }
        }
        return true;
    }
    public static List<FileVo> getFiles(String inFile) {
        List<FileVo> files = new ArrayList<>();

        File[] filesArr = new File(inFile).listFiles();
        for (File fobj : filesArr) {
            FileVo fileVo = new FileVo();
            if (fobj.isDirectory()) {
                fileVo.setFileType("1");
            } else if (fileEditable(fobj.getName())) {
                fileVo.setFileType("-1");
                fileVo.setSize(fobj.length());
            } else {
                fileVo.setFileType("0");
                fileVo.setSize(fobj.length());
            }
            String key = GU.getTimestamp();
            fileVo.setFileId(key);
            fileVo.setParentPath(fobj.getParent());
            fileVo.setFilePath("'" + fobj.getAbsolutePath
                    ().replace("\\", "\\\\") + "'");
            fileVo.setFilePathStr(fobj.getAbsolutePath())
            ;
            fileVo.setFileName(fobj.getName());
            fileVo.setDateStr(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(fobj.lastModified())) +
            "");
            files.add(fileVo);
        }
        return files;
    }
}
