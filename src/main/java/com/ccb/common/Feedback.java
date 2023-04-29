package com.ccb.common;

import com.alibaba.fastjson.JSON;

import java.util.Date;

public class Feedback<T> {
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_ERROR = 500;

    public static final String MESSAGE_SUCCESS = "操作成功";
    public static final String MESSAGE_ERROR = "操作失败";
    public static final String MESSAGE_ERROR_VALIDATE = "参数校验失败";


    private final int code;
    private final String msg;
    private final T data;

    private final Date serverTime = new Date();

    public Date getServerTime() {
        return serverTime;
    }

    private Feedback(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Feedback<T> of(int code, String msg, T data) {
        return new Feedback<>(code, msg, data);
    }

    public static <T> Feedback<T> success(String msg, T data) {
        return of(CODE_SUCCESS, msg, data);
    }

    public static <T> Feedback<T> success(T data) {
        return of(CODE_SUCCESS, MESSAGE_SUCCESS, data);
    }

    public static <T> Feedback<T> error(String msg) {
        return of(CODE_ERROR, msg, (T) null);
    }

    public static <T> Feedback<T> error(int code, String msg) {
        return of(code, msg, (T) null);
    }

    public static <T> Feedback<T> error(int code, T t) {
        return of(code, MESSAGE_ERROR, t);
    }

    public boolean isSuccess() {
        return getCode() == CODE_SUCCESS;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
