package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * @Author 李柯锦
 * @Date 2023/7/8 19:40
 * @Description 微信重定向的令牌
 */
@Slf4j
@Controller
public class WxLoginController {




//    @Autowired
//    WxAuthService wxAuthService;

    /*
     * @Description 扫码后进入该方法 最终返回重定向到登录页（登录成功并且保存到数据库）
     * @param code
     * @param state
     * @return String
     **/
    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}",code,state);
        //请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库

        // 根据code远程调用微信申请令牌，根据令牌查询用户信息，将用户信息写入本项目数据库
//        XcUser xcUser = wxAuthService.wxAuth(code);
        XcUser xcUser = null;
        if(xcUser==null){
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        String username = xcUser.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username="+username+"&authType=wx";
    }
}

