package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author 李柯锦
 * @Date 2023/7/3 14:41
 * @Description
 */

@Service
public class TeachplanServiceImpl implements TeachplanService{

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;


    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划的id判断新增还是修改
        Long teachplanId = saveTeachplanDto.getId();
        if(teachplanId == null){
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);

            //确定排序字段，找到同级节点个数，排序字段就是个数 + 1（同一个课程 同一级别的所有课程数量 + 1）
            Long parentid = saveTeachplanDto.getParentid();
            Long courseId = saveTeachplanDto.getCourseId();

            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getParentid,parentid);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            teachplan.setOrderby(count + 1);

            teachplanMapper.insert(teachplan);
        }else {
            //修改  有写没穿的 查出来 然后修改id

            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            //将参数赋值到teachplan中（有的属性被覆盖）
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Transactional
    @Override
    public void deleteTechPlan(Long id) {

        //判断是否有子节点
        List<Teachplan> teachplans = teachplanMapper.selectList(null);

        //判断是否有节点的parent_id等于id
        for(Teachplan teachplan : teachplans){
            if(teachplan.getParentid().equals(id)){
                 XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
        }

        //没有子节点删除
        teachplanMapper.deleteById(id);
    }

    @Transactional
    @Override
    public void moveupOrderBy(Long id) {
        //获取要修改的sortBy字段的值
        Teachplan teachplan = teachplanMapper.selectById(id);
        Integer orderby = teachplan.getOrderby();

        Integer realOrderBy = orderby - 1;
        //查询对应的realOrderBy的值。。
        if(realOrderBy == 0){
            XueChengPlusException.cast("该章节已经是最小排列了");
        }

        //查询出realOrderBy让其 + 1
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getOrderby,realOrderBy);
        queryWrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
        queryWrapper.eq(Teachplan::getParentid,teachplan.getParentid());
        Teachplan upTechPlan = teachplanMapper.selectOne(queryWrapper);
        upTechPlan.setOrderby(upTechPlan.getOrderby() + 1);
        teachplanMapper.updateById(upTechPlan);

        //将本teachplan - 1
        teachplan.setOrderby(realOrderBy);
        teachplanMapper.updateById(teachplan);


    }

    @Transactional
    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {

        //根据原来课程计划的id删除它所绑定的媒资。。

        //教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        //课程id
        Long courseId = teachplan.getCourseId();

        //先删除原来该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachplanId));


        //再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();

        //也可以直接拷贝
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

}
