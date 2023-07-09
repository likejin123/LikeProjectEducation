package com.xuecheng.ucenter.service;

/**
 * @Author 李柯锦
 * @Date 2023/7/8 19:59
 * @Description
 */

import com.xuecheng.ucenter.model.po.XcUser;

import java.util.Map;

/**
 * @author Mr.M
 * @version 1.0
 * @description 微信认证接口
 * @date 2023/2/21 22:15
 */
public interface WxAuthService {


    /*
     * @Description 调用微信获取用户数据（保存到数据库中）
     * @param code
     * @return XcUser
     **/
    public XcUser wxAuth(String code);


    /*
     * @Description 添加微信信息到数据库
     * @param userInfo_map
     * @return XcUser
     **/
    public XcUser addWxUser(Map userInfo_map);
}
