package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * @Author 李柯锦
 * @Date 2023/7/9 10:22
 * @Description
 */
public interface CourseTablesService  {
    /*
     * @Description 添加选课表
     * @param userId
     * @param courseId
     * @return XcChooseCourseDto
     **/
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);


    /*
     * @Description 判断学习资格
     * @param userId
     * @param courseId
     * @return XcCourseTablesDto
     **/
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    boolean saveChooseCourseStauts(String choosecourseId);


    /**
     * @description 我的课程表
     * @param params
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.learning.model.po.XcCourseTables>
     * @author Mr.M
     * @date 2022/10/27 9:24
     */
    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params);
}
