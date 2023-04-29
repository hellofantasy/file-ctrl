package com.ccb.controller;

import com.ccb.common.CommonUtils;
import com.ccb.common.Constant;
import com.ccb.common.Feedback;
import com.ccb.model.FileDto;
import com.ccb.model.FileVo;
import com.ccb.util.DateUtils;
import com.ccb.util.FileUtils;
import com.ccb.util.GU;
import com.ccb.util.ZipUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/files")
public class FileController {
    public static File TEMP_FILE_PATH = new File(Constant.USER_HOME + "temp");
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);

    @Value("${spring.fileProperty.deniedExtensions}")
    private String[] deniedExtensions;

    static {
        if (!TEMP_FILE_PATH.exists()) {
            TEMP_FILE_PATH.mkdirs();
        }
    }

    @PostMapping(value = "/getFiles")
    public Feedback getFiles(@RequestBody FileDto fileDto, HttpServletRequest request) throws ParseException {
        String sessionId = (String) request.getSession().getAttribute("sessionId");
        if (GU.isNull(sessionId)) {
            sessionId = UUID.randomUUID().toString();
            request.getSession().setAttribute("sessionId", sessionId);
        }
        List<FileVo> listFiles;
        if (GU.isNull(fileDto.getFilePath())) {
            String path = (String) request.getSession().getAttribute(sessionId);
            if (GU.isNull(path)) {
                fileDto.setFilePath(Constant.USER_HOME);
            } else {
                fileDto.setFilePath(path);
            }
        }
        String uploadPath = (String) request.getSession().getAttribute(sessionId + "_uploadPath");
        if (GU.isNull(uploadPath)) {
            uploadPath = Constant.USER_HOME + "upload";
        }
        request.getSession().setAttribute(sessionId + "_uploadPath", uploadPath);
        listFiles = FileUtils.getFiles(fileDto.getFilePath());
        if (GU.isNotNull(fileDto.getFileName())) {
            listFiles = listFiles.stream().filter(s -> s.getFileName().toLowerCase().contains(fileDto.getFileName().toLowerCase())).collect(Collectors.toList());
        }
        for (int i = 0; i < listFiles.size() - 1; i++) {
            for (int j = 0; j < listFiles.size() - i - 1; j++) {
                long time1 = simpleDateFormat.parse(listFiles.get(j).getDateStr()).getTime();
                long time2 = simpleDateFormat.parse(listFiles.get(j + 1).getDateStr()).getTime();
                if (time1 <= time2) {
                    FileVo temp = listFiles.get(j);
                    listFiles.set(j, listFiles.get(j + 1)
                    );
                    listFiles.set(j + 1, temp);
                }
            }
        }
        request.getSession().setAttribute(sessionId, fileDto.getFilePath());
        return Feedback.success(listFiles);
    }

    @PostMapping(value = "/getLastDir")
    public Feedback getLastDir(@RequestBody FileDto fileDto) {
        String lastPath = "";
        if (GU.isNotNull(fileDto.getFilePath())) {
            lastPath = new File(fileDto.getFilePath()).getParent();
        } else {
            lastPath = Constant.USER_HOME;
        }
        return Feedback.success(lastPath);
    }

    @PostMapping(value = "/checkDirExist")
    public Feedback checkDirExist(@RequestBody FileDto fileDto) {
        String lastPath = "";
        if (GU.isNotNull(fileDto.getFilePath())) {
            if (!new File(fileDto.getFilePath()).exists()) {
                return Feedback.error("文件夹路径不存在");
            }
            if (new File(fileDto.getFilePath()).isFile()) {
                return Feedback.error("请输入一个文件夹");
            }
            return Feedback.success("文件路径存在");
        } else {
            return Feedback.error("文件夹路径为空"
            );
        }
    }

    @PostMapping(value = "/deleteFile")
    public Feedback deleteFile(@RequestBody FileDto fileDto) throws IOException {
        if (GU.isNull(fileDto.getFilePath())) {
            return Feedback.error("文件不存在");
        }
        File file = new File(fileDto.getFilePath());
        if (!file.exists()) {
            return Feedback.error("文件不存在");
        }
        if (file.isDirectory()) {
            org.apache.commons.io.FileUtils.deleteDirectory(file);
        } else {
            file.delete();
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("时间：").append(DateUtils
                .getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS)).append(" 操作主机：").append(fileDto.getHostNameIP()).append(" ��行删除文件：").append(fileDto.getFilePath());
        CommonUtils.writeToFile(Constant.logsPath + "operationLog - " + DateUtils.getDateStr(DateUtils.YYYY_MM_DD) +
                ".log", stringBuffer.toString(), true);
        return Feedback.success("文件删除成功");
    }

    @GetMapping(value = "/downFile")
    public void downFile(HttpServletRequest request, HttpServletResponse response) {
        FileInputStream in = null;
        ServletOutputStream outputStream = null;
        try {
            File file = new File(request.getParameter("filePath"));
            if (!file.exists()) {
                response.getWriter().print("文件不存在");
                return;
            }
            if (file.isDirectory()) {
                FileOutputStream fileOutputStream = null;
                File tempFile = null;
                try {
                    tempFile = new File(TEMP_FILE_PATH +
                            File.separator + DateUtils.getDateStr(DateUtils.YYYYMMDDHHMMss) + ".zip");
                    fileOutputStream = new FileOutputStream(tempFile);
                    ZipUtils.toZip(file.getAbsolutePath()
                            , fileOutputStream, true);
                    response.setHeader("Content-Disposition", " attachment; filename = " + URLEncoder.encode(file.getName() + ".zip", "UTF-8"));
                    in = new FileInputStream(tempFile);
                    int length = 0;
                    byte[] buffer = new byte[1024];
                    outputStream = response.getOutputStream();
                    while ((length = in.read(buffer)) > 0
                            ) {
                        outputStream.write(buffer, 0, length);
                        outputStream.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            } else {
                response.setHeader("Content-Disposition",
                        "attachment;filename=" + URLEncoder.encode(file.getName(
                        ), "UTF-8"));
                in = new FileInputStream(file);
                int length = 0;
                byte[] buffer = new byte[1024];
                outputStream = response.getOutputStream()
                ;
                while ((length = in.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length)
                    ;
                    outputStream.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    @PostMapping(value = "fileUpload")
    public Feedback fileUpload(MultipartFile file, String targetPath, HttpServletRequest request) throws IOException {
        String sessionId = (String) request.getSession().
                getAttribute("sessionId");
        if (GU.isNull(sessionId)) {
            sessionId = UUID.randomUUID().toString();
            request.getSession().setAttribute("sessionId"
                    , sessionId);
        }
        String fileName = file.getOriginalFilename();

        String finalPath = targetPath + File.separator +
                fileName;
        File targetFile = new File(targetPath);
        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        if (GU.arrayToList(deniedExtensions).contains(fileExtension)) {
            return Feedback.error("黑名单文件类型，拒绝上传");
        }
        request.getSession().setAttribute(sessionId + "_uploadPath", targetPath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        if (new File(finalPath).exists()) {
            new File(finalPath).delete();
        }
        FileOutputStream out = null;
        InputStream in = null;
        try {
            in = file.getInputStream();
            int len = 0;
            byte[] buffer = new byte[102400];
            out = new FileOutputStream(finalPath);
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Feedback.error("上传失败。");
        } finally {
            in.close();
            out.flush();
            out.close();
        }
        if (new File(finalPath).exists()) {
            return Feedback.success("上传成功。");
        } else {
            return Feedback.error("上传失败。");
        }
    }

    @PostMapping(value = "fileMove")
    public Feedback fileMove(@RequestBody FileDto fileDto, HttpServletRequest request) throws IOException {
        try {
            String sessionId = (String) request.getSession().getAttribute("sessionId");
            if (GU.isNull(sessionId)) {
                sessionId = UUID.randomUUID().toString();
                request.getSession().setAttribute("sessionId", sessionId);
            }
            String[] sourceFiles = fileDto.getMoveSourceFile().split("\\|@\\|");
            for (int i = 0; i < sourceFiles.length; i++) {
                File moveFile = new File(sourceFiles[i]);
                if (!moveFile.exists()) {
                    return Feedback.error(moveFile.getAbsolutePath() + " 文件不存在。");
                }
                File targetFile = new File(fileDto.getMoveTargetPath());
                request.getSession().setAttribute(sessionId + "_uploadPath", fileDto.getMoveTargetPath());
                if (!targetFile.exists()) {
                    targetFile.mkdirs();
                }
                moveFile.renameTo(new File(fileDto.getMoveTargetPath() + File.separator + moveFile.getName()));
            }
            return Feedback.success("文件移动成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Feedback.error("文件移动失败");
        }
    }

    @PostMapping(value = "getServerTime")
    public Feedback fileMove() throws IOException {
        return Feedback.success(DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS));
    }

    @PostMapping(value = "getUploadPath")
    public Feedback getUploadPath(HttpServletRequest request) {
        try {
            String sessionId = (String) request.getSession().getAttribute("sessionId");
            if (GU.isNull(sessionId)) {
                sessionId = UUID.randomUUID().toString();
                request.getSession().setAttribute("sessionId", sessionId);
            }
            String uploadPath = (String) request.getSession().getAttribute(sessionId + "_uploadPath");
            if (GU.isNull(uploadPath)) {
                uploadPath = Constant.USER_HOME + "upload";
            }
            return Feedback.success(uploadPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Feedback.error("0%");
        } finally {
        }
    }

    @PostMapping(value = "/getFileInfo")
    public Feedback getFileInfo(@RequestBody FileDto fileDto) {
        String filePath = fileDto.getFilePath();
        String fileContent = CommonUtils.readFile(filePath);
        FileVo fileVo = new FileVo();
        fileVo.setFileContent(fileContent);
        return Feedback.success(fileVo);
    }

    @PostMapping(value = "/saveFile")
    public Feedback saveFile(@RequestBody FileDto fileDto) {
        CommonUtils.writeToFile(fileDto.getFilePath(), fileDto.getFileContent(), false);
        return Feedback.success("保存成功！");
    }
}