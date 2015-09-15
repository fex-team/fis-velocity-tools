package com.baidu.fis.web.util;

import java.util.Map;

public class ResponseData<T> {

    public static enum Status {
        SUCCESS(0),

        ERROR(1),

        WARN(2),

        REDIRECT(302);

        private int code = 0;

        private Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

    }

    private int code;

    private String msg;

    private T data = null;

    public ResponseData(Status status, T data) {
        this.code = status.code;

        switch (status){
            case SUCCESS:
                this.msg = "success";
            case ERROR:
                this.msg = "error";
            default:
               this.msg = "success";
        }

        this.data = data;
    }

    public ResponseData(Status status, String msg) {
        this.code = status.code;
        this.msg = msg;
    }

    public ResponseData(Status status, String msg, T data) {
        this.code = status.code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setStatus(Status status) {
        if (status != null) {
            this.code = status.code;
        }
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseData [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }

}
