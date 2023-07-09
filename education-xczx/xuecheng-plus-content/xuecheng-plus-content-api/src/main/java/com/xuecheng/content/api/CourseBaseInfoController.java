package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 李柯锦
 * @Date 2023/7/2 15:16
 * @Description
 */

//根路径配置到配置文件中

@RestController
@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;


    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")//指定权限标识符 拥有此权限才能访问
    public PageResult<CourseBase> list(
            PageParams pageParams,
            @RequestBody(required = false) QueryCourseParamsDto queryCourseParams){

        //当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();

        //用户所输的机构id
        String companyId = user.getCompanyId();

        //增加机构id的查询即可。。。
        return courseBaseInfoService.queryCourseBaseList(pageParams,queryCourseParams);

    }

    @ApiOperation("新增课程接口")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Inster.class}) AddCourseDto addCourseDto){


        Long companId = 1232141425L;

        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.createCourseBase(companId, addCourseDto);
        return courseBaseInfoDto;
    }

    @ApiOperation("根据课程id查询课程信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable("courseId") Long courseId){

//        //获取当前用户信息
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println(principal);

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user.getUsername());

        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfo(courseId);
        return courseBaseInfoDto;
    }


    @ApiOperation("修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){
        Long companId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.updateCourseBase(companId, editCourseDto);
        return courseBaseInfoDto;
    }


    //Java
    //delete  /course/87
    //87为课程id
    //请求参数：课程id
    //响应：状态码200，不返回信息

    @ApiOperation("删除课程")
    @DeleteMapping("/course/{id}")
    public void deleteCourseById(@PathVariable("id") Long id){
        Long companId = 1232141425L;
        courseBaseInfoService.deleteCourseById(companId,id);
    }



}
