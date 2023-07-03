package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @description 课程计划相关接口
 * @author Mr.M
 * @date 2022/9/6 21:42
 * @version 1.0
 */
public interface TeachplanService {

    /*
     * @Description 查询课程计划
     * @param courseId  课程id
     * @return List<TeachplanDto>
     **/
    public List<TeachplanDto> findTeachplanTree(long courseId);


    /*
     * @Description 新增 / 修改 /保存课程计划
     * @param teachplanDto
     * @return void
     **/
    public void saveTeachplan(SaveTeachplanDto teachplanDto);

    /*
     * @Description 根据课程计划id删除课程计划
     * @param id
     * @return void
     **/
    public void deleteTechPlan(Long id);

    /*
     * @Description 根据id减小orderby字段为1
     * @param id
     * @return void
     **/
    public void moveupOrderBy(Long id);
}
