package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @Author 李柯锦
 * @Date 2023/7/3 16:53
 * @Description
 */
public interface CourseTeacherService {

    /*
     * @Description 根据课程ID查询teacher
     * @param courseId 课程id
     * @return List<CourseTeacher>
     **/
    List<CourseTeacher> selectByCourseId(Long courseId);

    /*
     * @Description 插入课程教师
     * @param courseTeacher 课程教师数据
     * @return CourseTeacher
     **/
    CourseTeacher insert(CourseTeacher courseTeacher);
}
