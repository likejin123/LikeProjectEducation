package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author 李柯锦
 * @Date 2023/7/8 10:36
 * @Description
 */

public class MediaServiceClientFallback implements MediaServiceClient{
    @Override
    public String upload(MultipartFile filedata, String objectName) {
        //没有办法拿到发生熔断的异常
        return null;
    }
}
