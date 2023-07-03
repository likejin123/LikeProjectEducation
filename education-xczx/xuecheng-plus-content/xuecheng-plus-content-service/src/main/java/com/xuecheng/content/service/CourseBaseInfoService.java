package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @Author 李柯锦
 * @Date 2023/7/2 17:45
 * @Description 课程信息管理接口
 */
public interface CourseBaseInfoService {


    //课程分页查询
    /*
     * @Description 课程分页查询
     * @param pageParams 分页查询信息（1,2）
     * @param queryCourseParamsDto  查询条件（）
     * @return PageResult<CourseBase>
     **/
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);


    //新增课程
    /*
     * @Description 新增课程
     * @param companyId 机构id
     * @param addCourseDto 课程信息
     * @return CourseBaseInfoDto 课程详细信息
     **/
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);



    /*
     * @Description 根据课程id查询课程详细信息
     * @param courseId 课程id
     * @return CourseBaseInfoDto 课程详细信息
     **/
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);




    /*
     * @Description 修改课程
     * @param companyId 机构id
     * @param dto  修改课程信息
     * @return CourseBaseInfoDto 课程详细信息
     **/
    public CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto dto);

    /*
     * @Description 删除课程
     * @param companId 机构id
     * @param id 课程id
     * @return void
     **/
    void deleteCourseById(Long companId, Long id);
}
