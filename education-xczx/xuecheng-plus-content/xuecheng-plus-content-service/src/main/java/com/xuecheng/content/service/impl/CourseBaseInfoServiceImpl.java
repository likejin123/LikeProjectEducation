package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resources;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author 李柯锦
 * @Date 2023/7/2 17:47
 * @Description
 */

@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {


    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    /*
     * @Description 查询CouserBase的条件 + 分页查询
     * @param pageParams 分页查询参数
     * @param queryCourseParamsDto 查询条件
     * @return PageResult<CourseBase>  查询结果
     **/
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {


        //详细分页查询
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //根据名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        //根据审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());

        //创建分页查询对象 当前页码 每页记录数
        Page<CourseBase> courseBasePage = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<CourseBase> page = courseBaseMapper.selectPage(courseBasePage, queryWrapper);


        PageResult<CourseBase> pageResult = new PageResult<CourseBase>(page.getRecords(),page.getTotal(),pageParams.getPageNo(), pageParams.getPageSize());

        return pageResult;
    }


    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //参数合法性校验（不一定给controller校验，此处校验非常重要）
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }

        //向课程基本信息表course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        //将传入页面的参数放入里面
        BeanUtils.copyProperties(dto,courseBaseNew);

        courseBaseNew.setCompanyId(companyId);
        //设置审核状态（默认未提交）
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态（默认未发布）
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());

        //插入数据库
        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        if(insert<=0) {
            throw new RuntimeException("新增课程基本信息失败");
        }


        //向课程营销course_market写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        //课程id
        Long courseId = courseBaseNew.getId();
        //将页面数据拷入该对象
        BeanUtils.copyProperties(dto,courseMarketNew);
        courseMarketNew.setId(courseId);


        int i = saveCourseMarket(courseMarketNew);
        if(i<=0){
            throw new RuntimeException("保存课程营销信息失败");
        }


        //查询课程基本信息及营销信息并返回
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfo(courseId);
        return courseBaseInfoDto;
    }


    /*
     * @Description 保存营销信息
     * @param courseMarketNew 营销信息
     * @return int
     **/
    //单独写一个方法保存营销信息，存在则更新，不存在则天剑
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //参数合法性校验
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则没有选择");
        }
        //如果课程收费，但是没有填写价格抛出异常
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue()<=0){
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
            }
        }

        //存在则更新，不存在则插入
        //根据id从课程营销表查询
        Long id = courseMarketNew.getId();//主键
        CourseMarket courseMarketObj = courseMarketMapper.selectById(id);
        if(courseMarketObj == null){
            //插入
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            //更新
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    /*
     * @Description 课程id查询课程详细信息
     * @param courseId
     * @return CourseBaseInfoDto
     **/
    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){

        //从课程基本信息查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }

        //从课程营销表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //祖闯在一起
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }


        //查询分类名称，将分类名称放在courseBaseInfoDto对象中
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }


    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {

        //拿到课程id
        Long courseId = editCourseDto.getId();
        //查询课程信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        if(courseBase==null){
            XueChengPlusException.cast("课程不存在");
        }


        //数据合法性校验
        //根据具体的业务逻辑去校验
        //本机构只能修改本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }

        //封装数据
        BeanUtils.copyProperties(editCourseDto,courseBase);

        //修改时间
        courseBase.setChangeDate(LocalDateTime.now());

        //更新数据库课程基本信息
        int i = courseBaseMapper.updateById(courseBase);


        //封装营销信息的数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        saveCourseMarket(courseMarket);

        //查询课程信息
        CourseBaseInfoDto courseBaseInfoDto = this.getCourseBaseInfo(courseId);
        //更新数据
        return courseBaseInfoDto;
    }



    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Transactional
    @Override
    public void deleteCourseById(Long companId, Long id) {
        //判断机构是否为课程id 的机构
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if(!courseBase.getCompanyId().equals(companId)){
            XueChengPlusException.cast("不能删除其他机构的课程");
        }

        //开始根据课程id删除表中数据

        //删除课程基本信息表的数据
        courseBaseMapper.deleteById(id);

        //删除课程营销表的数据
        courseMarketMapper.deleteById(id);

        //删除课程对应的老师
        LambdaQueryWrapper<CourseTeacher> teacherQueryWrapper = new LambdaQueryWrapper<>();
        teacherQueryWrapper.eq(CourseTeacher::getCourseId,id);
        courseTeacherMapper.delete(teacherQueryWrapper);

        //删除课程计划表
        //查询出所有id的teachplan以便后续删除teachplanmedia
        LambdaQueryWrapper<Teachplan> teachplanQueryWrapper = new LambdaQueryWrapper<>();
        teachplanQueryWrapper.eq(Teachplan::getCourseId,id);
        teachplanMapper.delete(teachplanQueryWrapper);



        //根据课程计划表同时删除课程计划表对应的media（视频）
        LambdaQueryWrapper<TeachplanMedia> teachplanMediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanMediaLambdaQueryWrapper.eq(TeachplanMedia::getCourseId,id);
        teachplanMediaMapper.delete(teachplanMediaLambdaQueryWrapper);

    }
}
