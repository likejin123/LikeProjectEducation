package com.xuecheng.base.exception;

import java.io.Serializable;

/**
 * @Author 李柯锦
 * @Date 2023/7/3 11:28
 * @Description 和前端约定返回的异常信息的模型
 */
public class RestErrorResponse implements Serializable {

    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
