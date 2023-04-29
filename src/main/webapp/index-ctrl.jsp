<%@ page import="com.ccb.common.Constant" %>
<%@ page import="com.ccb.util.DateUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>文件管家</title>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
    <%@ page isELIgnored="false" %>
    <% String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String remoteHost = request.getRemoteHost();
        String version = Constant.version;
    %>
    <link type="text/css" rel="stylesheet" href="webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
    <script type="text/javascript" src="webjars/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript" src="webjars/jquery/3.6.0/jquery.js"></script>
    <script type="text/javascript" src="webjars/bootstrap/3.4.1/js/bootstrap.min.js"></script>
    <link type="text/css" rel="stylesheet" href="static/common/css/mystyle.css"/>
    <script type="text/javascript">
        var remoteHost = "<%=remoteHost%>";
        $(document).ready(function () {
            $("#selectAll").click(function () {
                var selectAll = $("#selectAll").prop("checked");
                $('input[name="taskId"]').each(function (i, o) {
                    $(this).prop('checked', selectAll);
                });
            })
            resetMsg();
            refreshData();
            $("#addTaskForm").click(function () {
                $("#btn_submit").show();
                $("#hostName_ip").hide();
                $("#addFormTaskId").val("")
                $("#myModalLabel").text("新增脚本");
                $("#addFormModal").modal();
            });
            $("#fileUpload").click(function () {
                var uploadPath;
                $.ajax({
                    url: '<%=basePath%>/files/getUploadPath',
                    method: "POST",
                    datetype: 'json',
                    // headers: {'content-type': 'application/json;charset=utf8'},
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
                formData.append("targetPath", targetPath);
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
            $("#btn_submit").click(function () {
                var taskId = $("#addFormTaskId").val()
                var taskName = $("#taskName").val()
                var taskDesc = $("#taskDesc").val()
                var shellContent = $("#shellContent").val()
                var runMsg = $("#runMsg").val()
                if (taskName == null || taskName == '') {
                    alert("脚本名称不为空")
                    return
                }

                if (shellContent == null || shellContent == '') {
                    alert("脚本内容不为空")
                    return
                }

                var unique = true;
                $.ajax({
                    url: '<%=basePath%>/task/checkUnique',
                    method: "POST",
                    datetype: 'json',
                    headers: {'content-type': 'application/json;charset=utf8'},
                    data: JSON.stringify({
                        "taskId": taskId,
                        "hostNameIP": remoteHost,
                        "taskName": taskName
                    }),
                    async: false,
                    success: function (result) {
                        if (result.code != 200) {
                            alert(result.msg)
                            unique = false;
                        }
                    }
                });
                if (!unique) {
                    return;
                }
                $.ajax({
                    url: '<%=basePath%>/task/saveTask',
                    method: "POST",
                    datetype: 'json',
                    headers: {'content-type': 'application/json;charset=utf8'},
                    data: JSON.stringify({
                        "taskId": taskId,
                        "taskDesc": taskDesc,
                        "taskName": taskName,
                        "shellContent": shellContent,
                        "runMsg": runMsg
                    }),
                    async: false,
                    success: function (result) {
                        if (result.code != 200) {
                            alert(result.msg)
                            return;
                        }
                        refreshData()
                        showSuccessMsg(result.data)
                        $("#addFormTaskId").val('');
                        $("#taskName").val('');
                        $("#taskDesc").val('');
                        $("#shellContent").val('');
                        $("#runMsg").val();
                    }
                });
            });
            $("#batchStopTask").click(function () {
                var taskIds = "";
                $('input[name="taskId"]').each(function (i, o) {
                    var taskId = $(this).val();
                    if ($(this).prop('checked') && taskId
                    ) {
                        taskIds += taskId + ",";
                    }
                });
                if (taskIds.length == 0) {
                    alert("请选择一条记录");
                    return false;
                }
                var stopConfirm = confirm("是否批量停止选中脚本？");
                if (!stopConfirm) {
                    return;
                }
                $.ajax({
                    url: '<%=basePath%>/task/stopTask',
                    method: "POST",
                    datetype: 'json',
                    headers: {'content-type': 'application/json;charset=utf8'},
                    data: JSON.stringify({"taskIds": taskIds}),
                    async: false,
                    success: function (result) {
                        if (result.code != 200) {
                            showErrorMsg(result.msg)
                            return;
                        }
                        refreshData()
                        showSuccessMsg(result.data)
                    }
                });
            });
            $("#reset_btn").click(function () {
                $("#search_taskName").val('');
                refreshData();
            });
            $("#btn_clear_log").click(function () {
                var confirms = confirm("是否清理当脚本的所有运行日志?");
                if (!confirms) {
                    return
                }
                var taskId = $("#logTaskId").val()
                $.ajax({
                    url: '<%=basePath%>/task/clearLogs',
                    method: "POST",
                    datetype: 'json',
                    headers: {'content-type': 'application/json;charset=utf8'},
                    data: JSON.stringify({"taskId": taskId}),
                    async: false,
                    success: function (result) {
                        if (result.code != 200) {
                            alert(result.msg)
                            return;
                        }
                        alert(result.data)
                        $("#logInfos").val("")
                    }
                });
            });
            $("#btn_clear_log_cur_date").click(function () {
                var taskId = $("#logTaskId").val()
                var logDate = $("#logDateSelect").val()
                var confirms = confirm("是否清理当前脚本" + logDate + "的运行日志?");
                if (!confirms) {
                    return
                }
                $.ajax({
                    url: '<%=basePath%>/task/clearLogs',
                    method: "POST",
                    datetype: 'json',
                    headers: {'content-type': 'application/json;charset=utf8'},
                    data: JSON.stringify({"taskId": taskId, "logDate": logDate}),
                    async: false,
                    success: function (result) {
                        if (result.code != 200) {
                            alert(result.msg)
                            return;
                        }
                        alert(result.data)
                        $("#logInfos").val("")
                    }
                });
            });
            setInterval(setServerTime, 1000);
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

        function startTask(taskId) {
            $.ajax({
                url: '<%=basePath%>/task/startTask',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"taskId": taskId, "hostNameIP": remoteHost}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    refreshData()
                    showSuccessMsg(result.data)
                }
            });
        }

        function stopOneTask(taskId) {
            var taskVo;
            $.ajax({
                url: '<%=basePath%>/task/getTask',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"taskId": taskId}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    taskVo = result.data;
                }
            });
            if (taskVo.status != 1) {
                alert("该脚本未启动中，无需停止！");
                return;
            }
            var stopConfirm = confirm("是否停止该脚本？");
            if (!stopConfirm) {
                return;
            }
            $.ajax({
                url: '<%=basePath%>/task/stopTask',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"taskIds": taskId}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    refreshData()
                    showSuccessMsg(result.data)
                }
            });
        }

        function delTask(taskId) {
            var taskVo;
            $.ajax({
                url: '<%=basePath%>/task/getTask',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"taskId": taskId}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    taskVo = result.data;
                }
            });
            if (taskVo.status == 1) {
                var delConfirm = confirm("该脚本正在运行中，是否强制删除？")
                if (!delConfirm) {
                    return;
                }
            } else {
                var delConfirm = confirm("是否删除该条脚本？")
                if (!delConfirm) {
                    return;
                }
            }
            $.ajax({
                url: '<%=basePath%>/task/delTask',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"taskId": taskId}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    refreshData()
                    showSuccessMsg(result.data)
                }
            });
        }

        function editTask(taskId) {
            var taskVo;
            $.ajax({
                url: '<%=basePath%>/task/getTask',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"taskId": taskId}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    taskVo = result.data;
                }
            });
            if (taskVo.status == 1) {
                alert("该脚本在运行中，不可编辑，请先停止脚本");
                return;
            }
            $("#addFormTaskId").val(taskVo.taskId);
            $("#taskName").val(taskVo.taskName);
            $("#taskDesc").val(taskVo.taskDesc);
            $("#shellContent").val(taskVo.shellContent);
            $("#runMsg").val(taskVo.runMsg);
            $("#hostName_ip").hide()
            $("#btn_submit").show();
            $("#myModalLabel").text("编辑脚本");
            $("#addFormModal").modal();
        }

        function viewTask(taskId) {
            var taskVo;
            $.ajax({
                url: '<%=basePath%>/task/getTask',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"taskId": taskId}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    taskVo = result.data;
                }
            });
            $("#addFormTaskId").val(taskVo.taskId);
            $("#taskName").val(taskVo.taskName);
            $("#taskDesc").val(taskVo.taskDesc);
            $("#shellContent").val(taskVo.shellContent);
            $("#runMsg").val(taskVo.runMsg);
            $("#hostName_ip").show()
            $("#btn_submit").hide();
            $("#myModalLabel").text("查看脚本");
            $("#addFormModal").modal();
        }

        function refreshData() {
            resetMsg();
            var taskInfos;
            document.getElementById("taskContents").innerHTML = "";
            $("#selectAll").prop("checked", false);
            var search_taskName = $("#search_taskName").val()
            $.ajax({
                url: '<%=basePath%>/task/taskIndex',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({
                    "taskName": search_taskName
                }),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    taskInfos = result.data
                    if (taskInfos == null) {
                        return;
                    }
                    var contents = '';
                    for (var i = 0; i < taskInfos.length;
                         i++) {
                        var obj = taskInfos[i];
                        contents += ' <tr> ';
                        contents += '<td class="pull-center"> <input type="checkbox" value="' + obj.taskId + '" name="taskId" id="' + obj.taskId + '"/>';
                        contents += ' </td>';
                        contents += ' <td class="pull-center">' + (i + 1) + '</td> '
                        contents += ' <td class="pull-center"><a href="#" onclick="viewTask(' + obj.taskId + ')">' + obj.taskName + '</a></td>'
                        contents += ' <td class="pull-center">' + obj.lastRuntime + '</td> '
                        contents += ' <td class="pull-center"><a href="#" onclick="viewLogs(' + obj.taskId + ')">查看日志</a></td>'
                        contents += ' <td class="pull-center">'
                        if (obj.status == 1) {
                            contents += '<button class="btn btn-success" title="启动" disabled onclick="startTask(' + obj.taskId + ')"> ' +
                                '<i class="glyphicon glyphicon-play"></i> 启动</button>'
                            contents += ' <button class="btn btn-danger" title="停止"  onclick="stopOneTask(' + obj.taskId + ')">' +
                                '<i class="glyphicon glyphicon-stop"></i> 停止</button>'
                            contents += ' <button class="btn btn-info" title="编辑" disabled  onclick="editTask(' + obj.taskId + ')">' + '<i class="glyphicon glyphicon-edit"></i> 编辑</button>'
                            contents += ' <button class="btn btn-danger" title="删除" disabled  onclick="delTask(' + obj.taskId + ')">' + '<i class="glyphicon glyphicon-trash"></i> 删除</button>'
                        } else {
                            contents += '<button class="btn btn-success" title="启动" onclick="startTask(' + obj.taskId + ')"> ' + '<i class="glyphicon glyphicon-play"></i> 启动</button>'
                            contents += ' <button class="btn btn-danger" title="停止" disabled  onclick="stopOneTask(' + obj.taskId + ')">' + '<i class="glyphicon glyphicon-stop"></i> 停止</button>'
                            contents += ' <button class="btn btn-info" title="编辑"  onclick="editTask(' + obj.taskId + ')">' + '<i class="glyphicon glyphicon-edit"></i> 编辑</button>'
                            contents += ' <button class="btn btn-danger" title="删除"  onclick="delTask(' + obj.taskId + ')">' + '<i class="glyphicon glyphicon-trash"></i> 删除</button>'
                        }
                        contents += '</td>'
                        contents += '</tr>'
                    }
                    document.getElementById("taskContents").innerHTML = contents;
                    showSuccessMsg("脚本列表刷新成功。")
                }
            });
        }

        function viewLogs(taskId) {
            var logVo;
            var logs;
            var logDateList;
            $.ajax({
                url: '<%=basePath%>/task/viewRunLog',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"taskId": taskId}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    logVo = result.data;
                    logs = logVo.logs;
                    $("#logInfos").val(logs)
                    logDateList = logVo.logDateList;
                    var contents = ""
                    document.getElementById("logDateSelect").innerHTML = ""
                    for (var i = 0; i < logDateList.length; i++) {
                        if (i == 0) {
                            contents += '<option value="' + logDateList[i] + '" selected>' + logDateList[i] + '</option> '
                        } else {
                            contents += '<option value="' + logDateList[i] + '" >' + logDateList[i] + '</option> '
                        }
                    }
                    document.getElementById("logDateSelect").innerHTML = contents;
                }
            });
            $("#viewLogFormModal").modal();
            $("#logTaskId").val(taskId)
        }

        function searchLog() {
            var logVo;
            var logs;
            var logTaskId = $("#logTaskId").val()
            var logDate = $("#logDateSelect").val()
            $.ajax({
                url: '<%=basePath%>/task/viewRunLog',
                method: "POST",
                datetype: 'json',
                headers: {'content-type': 'application/json;charset=utf8'},
                data: JSON.stringify({"taskId": logTaskId, "logDate": logDate}),
                async: false,
                success: function (result) {
                    if (result.code != 200) {
                        showErrorMsg(result.msg)
                        return;
                    }
                    logVo = result.data;
                    logs = logVo.logs;
                }
            });
            $("#logInfos").val(logs)
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

        function resetMsg() {
            $("#succesImg").css("display", "none");
            $("#errorImg").css("display", "none");
            document.getElementById("showSuccessText").innerHTML = "";
            document.getElementById("showErrorText").innerHTML = "";
        }
    </script>
</head>
<body>
<div class="wrapper wrapper-content">
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title" style="font-size: 18px;float: right" id="serverTime">
                服务器时间:<%=DateUtils.getDateStr(DateUtils.YYYY_MM_DD_HH_MM_SS)%>
            </h3>
            <h3 class="panel-title" style="font-size: 36px"><a href="index-ctrl.jsp">文件管家
            </a></h3>
        </div>
        <div class="panel-body">
            <!-- 搜索 -->
            <div id="search-collapse" class="collapse">
            </div>
            <!-- 工具栏 -->
            <div id="toolbar">
                <div style="display: inline;" title="新增脚本">
                    <button class="btn btn-info" id="addTaskForm">
                        <i class="glyphicon glyphicon-plus"></i> 新增脚本
                    </button>
                </div>
                <div style="display: inline;" title="刷新脚本">
                    <button class="btn btn-warning" onclick="window.location.reload(true);">
                        <i class="glyphicon glyphicon-refresh"></i> 刷新脚本
                    </button>
                </div>
                <div style="display: inline;" title="文件上传">
                    <a href="#" class="btn btn-success" id="fileUpload">
                        <i class="glyphicon glyphicon-upload"></i> 文件上传
                    </a>
                </div>
                <div style="display: inline;" title="文件下载">
                    <a href="files.jsp" class="btn btn-success" id="fileDownload">
                        <i class="glyphicon glyphicon-download"></i> 文件下载
                    </a>
                </div>
                <%--<div style="display: inline;" title="重启应用">
                    <a href="#" class="btn btn-success" id="restartServer">
                        <i class="glyphicon glyphicon-repeat"></i> 重启应用
                    </a>
                </div>--%>
                <div style="display: inline;margin-left:90px">
                    <img id="succesImg" style="display: none" src="static/images/checkmark_status.gif"
                         alt="消息图标 - 成功 ">
                    <img id="errorImg" src="static/images/error_status.gif" style="display: none"
                         alt="消息图标 - 失败 ">
                    <label id="showSuccessText" class="successCss"></label>
                    <label id="showErrorText" class="errorCss"></label>
                </div>
            </div>
            <div>
                <div class="pull-right" title=" 查询">
                    <label for="search_taskName">脚本名称：</label>
                    <input type="text" id="search_taskName">
                    <button class="btn btn-success" onclick="refreshData()">
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
                    <th class="pull-center">脚本名称</th>
                    <th class="pull-center">最近运行时间</th>
                    <th class="pull-center">运行日志</th>
                    <th class="pull-center">操作</th>
                </tr>
                </thead>
                <tbody id="taskContents" class="table-str
iped  table-hover">
                </tbody>
            </table>
        </div>
    </div>
</div>
<div class="modal fade" id="fileUploadModal" tabindex="-1
" role="dialog" aria-labelledby="myfileUploadModalLabel">
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
                            <label for="targetPath"><span
                                    style="color: red;font-weight: bolder">*</span>目标路径</label>
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
<div class="modal fade" id="addFormModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div style="width: 100%;height: 100%">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="close"><span
                            aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" id="myModalLabel"> 新增 </h4>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="addFormTaskId" id="addFormTaskId">
                    <div class="form-group">
                        <label for="taskName"><span style="color: red;font-weight: bolder">*</span>脚本名称</label>
                        <input type="text" name="taskName" class="form-control" id="taskName" placeholder="脚本名称">
                    </div>

                    <div class="form-group">
                        <label for="shellContent"><span style="color: red;font-weight: bolder">*</span>脚本内容</label>
                        <textarea name="shellContent" rows="10" class="form-control" id="shellContent" content=""
                                  placeholder="脚本内容"></textarea>
                    </div>
                    <div class="form-group">
                        <label for="taskDesc">脚本描述</label>
                        <textarea name="taskDesc" rows="3" class="form-control" id="taskDesc" content=""
                                  placeholder="脚本描述"></textarea>
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
<div class="modal fade" id="viewLogFormModal" tabindex="-1" role="dialog" aria-labelledby="viewLogFormLabel">
    <div style="width: 100%;height: 100%">
        <div class="modal-dialog" role="document">
            <input type="hidden" id="logTaskId">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" d
                            ata-dismiss="modal" aria-label="close"><span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" id="viewLogFormLabel">查看运行日志</h4>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label for="enable"><span style="color: red;font-weight: bolder">*</span>选择日期</label>
                        <select id="logDateSelect" onchange="searchLog()" class="selectCss" style="width: 120px">
                        </select>
                        <textarea name="logInfos" rows="20" readonly="readonly" class="form-control" id="logInfos"
                                  content=""></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <span class="glyphicon close" aria-hidden="true"></span>关闭
                    </button>
                    <button type="button" id="btn_clear_log_cur_date" class="btn btn-danger" data-dismiss="modal">
                        <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>清除当天
                    </button>
                    <button type="button" id="btn_clear_log" class="btn btn-danger" data-dismiss="modal">
                        <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>清空日志
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>