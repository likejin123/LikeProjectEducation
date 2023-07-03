package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 李柯锦
 * @Date 2023/7/3 11:31
 * @Description
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /*
     * @Description 自定义异常
     * @param e
     * @return RestErrorResponse
     **/
    @ResponseBody
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e) {
        log.error("【系统异常】{}",e.getErrMessage(),e);
        return new RestErrorResponse(e.getErrMessage());

    }

    /*
     * @Description 系统异常
     * @param e
     * @return RestErrorResponse
     **/
    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e) {

        //记录日志异常
        log.error("【系统异常】{}",e.getMessage(),e);

        //返回前端结果
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());

    }


    /*
     * @Description 解析 JSR303 参数校验异常
     * @param e
     * @return RestErrorResponse
     **/
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodJSR303(MethodArgumentNotValidException e) {

        //获取框架绑定的异常信息
        BindingResult bindingResult = e.getBindingResult();

        //存放错误信息
        List<String> msgList = new ArrayList<>();


        //将错误信息放在msgList
        bindingResult.getFieldErrors().stream().forEach(item->msgList.add(item.getDefaultMessage()));
        //拼接错误信息
        String msg = StringUtils.join(msgList, ",");


        log.error("【系统异常】{}",msg);
        return new RestErrorResponse(msg);

    }
}
