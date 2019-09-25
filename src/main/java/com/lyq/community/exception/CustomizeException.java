package com.lyq.community.exception;

/**
 * Created by codedrinker on 2019/5/28.
 */
public class CustomizeException extends RuntimeException {
    private String message;
    private Integer code;

    public CustomizeException(ICustomizeErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public CustomizeException(String s) {
        super(s);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }
}
