package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author 李柯锦
 * @Date 2023/7/3 16:50
 * @Description 教师管理接口
 */

@Api(tags = "教师管理接口")
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;


    //Java
    //get /courseTeacher/list/75
    //75为课程id，请求参数为课程id
    //
    //响应结果
    //[{"id":23,"courseId":75,"teacherName":"张老师",
    // "position":"讲师","introduction":"张老师教师简介张老师教师简介张老师教师简介张老师教师简介",
    // "photograph":null,"createDate":null}]


    @ApiOperation("根据课程id查询教师")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> selectByCourseId(@PathVariable("courseId") Long courseId){
        List<CourseTeacher> result = courseTeacherService.selectByCourseId(courseId);
        return result;
    }


    //post  /courseTeacher
    //
    //请求参数：
    //{
    //  "courseId": 75,
    //  "teacherName": "王老师",
    //  "position": "教师职位",
    //  "introduction": "教师简介"
    //}
    //响应结果：
    //{"id":24,"courseId":75,"teacherName":"王老师","position":"教师职位","introduction":"教师简介","photograph":null,"createDate":null}

    @ApiOperation("添加教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher insertCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        CourseTeacher courseteacher = courseTeacherService.insert(courseTeacher);
        return courseTeacher;
    }


}
