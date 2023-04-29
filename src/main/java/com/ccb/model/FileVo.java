package com.ccb.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileVo {
    private String fileId;
    private String parentPath;
    private String filePath;
    private String filePathStr;
    private String fileName;
    private String fileContent;
    private String resourcePath;
    private String dateStr;
    private String hostNameIP;
    private long size;
    // 1 文件夹 0 文件
    private String fileType;
    private String searchFilePath;
    private String moveSourceFile;
    private String moveTargetPath;

}
