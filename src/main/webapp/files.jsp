<%@ page import="com.ccb.common.Constant" %>
<%@ page import="com.ccb.util.DateUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title><%=request.getServerName()%>--文件下载</title>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
    <%@ page isELIgnored="false" %>
    <%
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String remoteHost = request.getRemoteHost();
        String version = Constant.version;
    %>
    <link type="text/css" rel="stylesheet" href="webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
    <script type="text/javascript" src="webjars/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="webjars/jquery/3.6.0/jquery.js"></script>
    <script type="text/javascript" src="webjars/bootstrap/3.4.1/js/bootstrap.min.js"></script>
    <link type="text/css" rel="stylesheet" href="static/common/css/mystyle.css"/>
    <script type="text/javascript">
        var globalFilePath = ""
        var remoteHost = "<%=remoteHost%>";
        $(document).ready(function () {
            $("#selectAll").click(function () {
                var selectAll = $("#selectAll").prop("checked");
                $('input[name="fileId"]').each(function (i, o) {
                    $(this).prop('checked', selectAll);
                });
            })
            getFiles("");
            $("#lastDir").click(function () {
                $.ajax({
                    url: '<%=basePath%>/files/getLastDir',
                    method: "POST",
                    datetype: 'json',
                    headers: {'content-type': 'application/json;charset=utf8'},
                    data: JSON.stringify({
                        "filePath": globalFilePath,
                        "fileId": '',
                        "hostNameIP": remoteHost,
                        "fileName": ''
                    }),
                    async: false,
                    success: function (result) {
                        if (result.code != 200) {
                            alert(result.msg)
                        }
                        var path = result.data
                        globalFilePath = path
                        getFiles(globalFilePath)
                    }
                });
            });
            $("#btn_submit").click(function () {
                var fileContent = $("#fileContent").val()
                var filePath = $("#filePath").val()
                if (filePath == null || filePath == '') {
                    alert("文件路径不为空")
                    return
                }
                $.ajax({
                    url: '<%=basePath%>/files/saveFile',
                    method: "POST",
                    datetype: 'json',
                    headers: {'content-type': 'application/json;charset=utf8'},
                    data: JSON.stringify({
                        "filePath": filePath,
                        "fileContent": fileContent
                    }),
                    async: false,
                    success: function (result) {
                        if (result.code != 200) {
                            alert(result.msg)
                            return;
                        }
                        window.location.reload(true);
                        showSuccessMsg(result.data)
                        $("#filePath").val('');
                        $("#fileContent").val('');
                    }
                });
            });
            $("#reset_btn").click(function () {
                $("#search_fileName").val('')
                getFiles("")
            });
            $("#toDir").click(function () {
                var curDir = $("#curDir").val()
                $.ajax({
                    url: '<%=basePath%>/files/checkDirExist',
                    method: "POST",
                    datetype: 'json',
                    headers: {'content-type': 'application/json;charset=utf8'},
                    data: JSON.stringify({
                        "filePath": curDir,
                    }),
                    async: false,
                    success: function (result) {
                        if (result.code != 200) {
                            alert(result.msg)
                        } else {
                            getFiles(curDir)
                        }
                    }
                });
            });
            $("#fileUpload").click(function () {
                var uploadPath;
                $.ajax({
                    url: '<%=basePath%>/files/getUploadPath',
                    method: "POST",
                    datetype: 'json',
                    data: JSON.stringify({}),
                    async: false,
                    success: function (result) {
                        uploadPath = result.data;
                    }
                });
                $("#targetPath").val(uploadPath);
                $("#fileUploadModal").modal();
            });
            $("#file_submit").click(function () {
                var targetPath = $("#targetPath").val();
                var file = $("#file")[0].files[0];
                if (targetPath == null || targetPath == '') {
                    alert("目标存放路径不为空")
                    return;
                }
                if (file == null || file == '') {
                    alert("上传文件不为空")
                    return;
                }
                var formData = new FormData();
                formData.append("file", file);
                formData.append("targetPath", targetPath)
                ;
                $.ajax({
                    url: '<%=basePath%>/files/fileUpload',
                    method: "POST",
                    datetype: 'json',
                    contentType: false,
                    processData: false,
                    // headers: {'content-type': 'application/json;charset=utf8'},
                    data: formData,
                    async: true,
                    success: function (result) {
                        alert(result.msg)
                        window.location.reload(true);
                        return;
                    }
                });
            });
            $("#fileMoveBtn").click(function () {
                var filePaths = "";
                $('input[name="fileId"]').each(function (i, o) {
                    var filePath = $(this).val();
                    if ($(this).prop('checked') && filePath) {
                        filePaths += filePath + "|@|";
                    }
                });
                var uploadPath;
                $.ajax({
                    url: '<%=basePath%>/files/getUploadPath',
                    method: "POST",
                    datetype: 'json',
                    data: JSON.stringify({}),
                    async: false,
                    success: function (result) {
                        uploadPath = result.data;
                    }
                });
                $("#moveSourceFile").val(filePaths);
                $("#moveTargetFile").val(uploadPath);
                $("#fileMoveModal").modal();
            });
            $("#file_Move").click(function () {
                var moveSourceFile = $("#moveSourceFile").val();
                var moveTargetFile = $("#moveTargetFile").val();
                if (moveSourceFile == null || moveSourceFile == '') {
                    alert("源文件不为空");
                    return
                }
                if (moveTargetFile == null || moveTargetFile == '') {
                    alert("目标存放路径不为空");
                    return
                }
                var files = $("#moveSourceFile").val().split("|@|");
                var fileObjs = "是否将文件：\n";
                for (var i = 0; i < files.length; i++) {
                    if (files[i]) {
                        fileObjs += files[i] + ";\n";
                    }
                    if (files[i] == moveTargetFile) {
                        alert("无法将" + files[i] + " 移动到 " + moveTargetFile);
                    }
                }
                fileObjs += " 移动到：" + moveTargetFile;
                var confirms = confirm(fileObjs);
                if (!confirms) {
                    return;
                }
                $.ajax({
                    url: '<%=basePath%>/files/fileMove',
                    method: "POST",
                    datetype: 'json',
                    headers: {'content-type': 'application/json;charset=utf8'},
                    data: JSON.stringify({"moveSourceFile": moveSourceFile, "moveTargetPath": moveTargetFile}),
                    async: false,
                    success: function (result) {
                        alert(result.msg)
                        window.location.reload(true);
                    }
                });
            });
            setInterval(setServerTime, 1000)
        });

        function setServerTime() {
            $.ajax({
                url: '<%=basePath%>/files/getServerTime',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({}),
                async: false,
                success: function (result) {
                    document.getElementById("serverTime").innerHTML = "服务器时间:" + result.data
                }
            });
        }

        function getFiles(filePath) {
            if (filePath) {
                globalFilePath = filePath
            }
            var search_fileName = $("#search_fileName").val()
            $.ajax({
                url: '<%=basePath%>/files/getFiles',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({
                    "filePath": filePath,
                    "fileId": '',
                    "hostNameIP": remoteHost,
                    "fileName": search_fileName
                }),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        alert(result.msg)
                    }
                    var contents = '';
                    var listFiles = result.data
                    if (listFiles == null) {
                        return;
                    }
                    var contents = '';
                    for (var i = 0; i < listFiles.length;
                         i++) {
                        var obj = listFiles[i];
                        globalFilePath = obj.parentPath
                        contents += ' <tr> ';
                        contents += '<td class="pull-center"> <input type="checkbox" value="' + obj.filePathStr + '" name="fileId" id="' + obj.fileId + '"/>';
                        contents += ' </td>';
                        contents += ' <td class="pull-center">' + (i + 1) + '</td> '
                        if (obj.fileType != "1") {
                            contents += ' <td class="pull-center">'
                            contents += '<a href="javascript:void(0);" title="下载文件：' + obj.filePathStr + '" onclick="downloadFile(' + obj.filePath + ')">' + obj.fileName + ' </a>';
                            contents += '</td> '
                        } else {
                            contents += ' <td class="pull-center">'
                            contents += '<a href="javascript:void(0);" title="进入文件夹：' + obj.filePathStr + '"onclick="getFiles(' + obj.filePath + ')">' + obj.fileName + ' </a>';
                            contents += '</td> '
                        }
                        contents += ' <td class="pull-center">' + obj.dateStr + '</td>'
                        if (obj.fileType != "1") {
                            contents += ' <td class="pull-center">' + obj.size + '</td>'
                        } else {
                            contents += ' <td class="pull-center"></td>'
                        }
                        contents += ' <td class="pull-center">' + obj.filePathStr + '</td> '
                        contents += ' <td class="pull-center">'
                        if (obj.fileType == "1") {
                            contents += '<button class="btn btn-success" onclick="downloadFile(' + obj.filePath + ')" title="下载文件夹:' + obj.filePathStr + '"  > <i class="glyphicon glyphicon-download-alt">'
                            contents += '</i>  下载</button>'
                        } else if (obj.fileType == "-1") {
                            contents += '<button class="btn btn-info" onclick="editFile(' + obj.filePath + ')" title="编辑文件:' + obj.filePathStr + '"  > <i class="glyphicon glyphicon-edit">'
                            contents += '</i>  编辑</button>'
                        }
                        contents += ' <button class="btn btn-danger" title="删除"  onclick="delFile(' + obj.filePath + ')">' + '<i class="glyphicon glyphicon-trash"></i>  删除</button>'
                        contents += '</td>'
                        contents += '</tr>'
                    }
                    document.getElementById("taskContents").innerHTML = contents;
                    $("#curDir").val(globalFilePath);
                    showSuccessMsg("文件列表刷新成功。")
                }
            });
        }

        function delFile(filePath) {
            var delConfirm = confirm("是否删除文件"
                + filePath + "？")
            if (!delConfirm) {
                return;
            }
            $.ajax({
                url: '<%=basePath%>/files/deleteFile',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({
                    "filePath": filePath,
                    "fileId": '',
                    "hostNameIP": remoteHost,
                    "fileName": ''
                }),
                async: false,
                success: function (result) {
                    getFiles(globalFilePath)
                    if (result.code == 200) {
                        showSuccessMsg("文件删除成功。")
                    } else {
                        showErrorMsg("文件删除失败")
                    }
                }
            });
        }

        function downloadFile(filePath) {
            window.location.href = '<%=basePath%>/files/downFile?filePath=' + encodeURIComponent(filePath)
        }

        function editFile(filePath) {
            var fileVo;
            $.ajax({
                url: '<%=basePath%>/files/getFileInfo',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"filePath": filePath}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    fileVo = result.data;
                }
            });
            $("#fileContent").val(fileVo.fileContent);
            $("#filePath").val(filePath);
            $("#myModalLabel").text("编辑文件");
            $("#addFormModal").modal();
        }

        function showSuccessMsg(msg) {
            $("#errorImg").css("display", "none");
            $("#succesImg").css("display", "inline");
            document.getElementById("showSuccessText").innerHTML = msg;
            document.getElementById("showErrorText").innerHTML = "";
        }

        function showErrorMsg(msg) {
            $("#succesImg").css("display", "none");
            $("#errorImg").css("display", "inline");
            document.getElementById("showErrorText").innerHTML = msg;
            document.getElementById("showSuccessText").innerHTML = "";
        }
    </script>
</head>
<body>
<div id="mask"></div>
<div class="wrapper wrapper-content">
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title" style="font-size: 18px;float: right" id="serverTime">
                服务器时间:<%=DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS)%>
            </h3>
            <h3 class="panel-title" style="font-size: 36px"><a href="files.jsp"><%=request.getServerName()%>
                文件下载
            </a></h3>
        </div>
        <div class="panel-body">
            <!-- 搜索 -->
            <div id="search-collapse" class="collapse">
            </div>
            <!-- 工具栏 -->
            <div id="toolbar">
                <div style="display: inline;" title="首页">
                    <a href="index-ctrl.jsp" class="btn btn-success" id="fileDownload">
                        <i class="glyphicon glyphicon-home"></i> 首页
                    </a>
                </div>
                <div style="display: inline;" title="文件上传">
                    <a href="#" class="btn btn-success" id="fileUpload">
                        <i class="glyphicon glyphicon-upload"></i> 文件上传
                    </a>
                </div>
                <div style="display: inline;" title="移动文件">
                    <a href="#" class="btn btn-success" id="fileMoveBtn">
                        <i class="glyphicon glyphicon-move"></i> 移动文件
                    </a>
                </div>
                <div style="display: inline;" title="上一级">
                    <button class="btn btn-info" id="lastDir">
                        <i class="glyphicon glyphicon-arrow-up"></i> 上一级
                    </button>
                </div>
                <div style="display: inline;" title="当前路径">
                    <input type="text" id="curDir" width="50">
                    <button class="btn btn-info" id="toDir">
                        <i class="glyphicon glyphicon-arrow-right"></i> 跳转
                    </button>
                </div>
                <div style="display: inline;margin-left:90px">
                    <img id="succesImg" style="display: none"
                         src="static/images/checkmark_status.gif"
                         alt="消息图标 - 成功 ">
                    <img id="errorImg" src="static/images/error_status.gif" style="display: none"
                         alt="消息图标 - 失败 ">
                    <label id="showSuccessText" class="successCss"></label>
                    <label id="showErrorText" class="errorCss"></label>
                </div>
            </div>
            <div>
                <div class="pull-right" title=" 查询">
                    <label for="search_fileName">文件名字：</label>
                    <input type="text" id="search_fileName">
                    <button class="btn btn-success" onclick="getFiles('')">
                        <i class="glyphicon glyphicon-search"></i> 查询
                    </button>
                    <button class="btn btn-primary" type="reset" id="reset_btn">
                        <i class="glyphicon glyphicon-refresh"></i> 重置
                    </button>
                </div>
            </div>
        </div>
        <div>
            <table class="table">
                <thead>
                <tr>
                    <th class="pull-center">
                        <input type="checkbox" id="selectAll"/>
                    </th>
                    <th class="pull-center">序 号</th>
                    <th class="pull-center">文件名称</th>
                    <th class="pull-center">最近修改时间
                    </th>
                    <th class="pull-center">文件大小(B)</th>
                    <th class="pull-center">文件绝对路径</th>
                    <th class="pull-center">操作</th>
                </tr>
                </thead>
                <tbody id="taskContents" class="table-striped  table-hover">
                </tbody>
            </table>
        </div>
    </div>
</div>
<div class="modal fade" id="addFormModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div style="width: 100%;height: 100%">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="close"><span
                            aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" id="myModalLabel"> 编辑 </h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label for="filePath"><span style="color: red;font-weight: bolder">*</span>文件路径</label>
                        <input type="text" name="filePath" class="form-control" id="filePath"
                               placeholder="文件路径">
                    </div>
                    <div class="form-group">
                        <label for="fileContent"><span style="color: red;font-weight: bolder"></span>文件内容</label>
                        <textarea name="fileContent" rows
                                ="18" class="form-control" id="fileContent" content="" placeholder="文件内容"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>关闭
                    </button>
                    <button type="button" id="btn_submit"
                            class="btn btn-primary" data-dismiss="modal">
                        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span>保存
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="fileUploadModal" tabindex="-1" role="dialog" aria-labelledby="myfileUploadModalLabel">
    <div style="width: 100%;height: 100%">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="close"><span
                            aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title"> 文件上传
                    </h4>
                </div>
                <form enctype="multipart/form-data" id="fileUploadForm">
                    <div class="modal-body">
                        <div class="form-group">
                            <label for="file"><span style="color: red;font-weight: bolder">*</span>选择文件 </label>
                            <input type="file" name="file" class="form-control" id="file" placeholder="文件路径">
                            <label for="targetPath"><span style="color: red;font-weight: bolder">*</span>目标路径</label>
                            <input type="text" name="targetPath" class="form-control" id="targetPath"
                                   value="/home/ap/ccda/upload/"
                                   placeholder="目标路径">
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">
                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>关 闭
                            </button>
                            <button type="button" id="file_submit" class="btn btn-primary" data-dismiss="imodal">
                                <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span>上 传
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="fileMoveModal" tabindex="-1" role="dialog" aria-labelledby="myfileMoveModalLabel">
    <div style="width: 100%;height: 100%">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="close"><span
                            aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title"> 文件移动
                    </h4>
                </div>
                <form enctype="multipart/form-data" id="fileMoveForm">
                    <div class="modal-body">
                        <div class="form-group">
                            <label for="file"><span style="color: red;font-weight: bolder">*</span>选择文件 </label>
                            <input type="text" name="moveFile" class="form-control" id="moveSourceFile"
                                   placeholder="文件路径">
                            <label for="targetPath"><span style="color: red;font-weight: bolder">*</span>目标路径</label>
                            <input type="text" name="targetPath" class="form-control" id="moveTargetFile"
                                   value="/home/ap/ccda/upload/"
                                   placeholder="目标路径">
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">
                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>关 闭
                            </button>
                            <button type="button" id="file_Move" class="btn btn-primary" data-dismiss="imodal">
                                <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span>移 动
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>