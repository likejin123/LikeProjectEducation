package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @Author 李柯锦
 * @Date 2023/7/2 15:12
 * @Description 课程查询条件模型类
 */

@Data
@ToString
public class QueryCourseParamsDto {

    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;

}

