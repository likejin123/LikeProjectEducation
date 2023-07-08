package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author 李柯锦
 * @Date 2023/7/3 14:11
 * @Description 课程计划管理相关接口
 */

@Api(tags = "课程计划管理接口")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        return teachplanTree;
    }

    @ApiOperation("课程计划章节新增或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
        return;
    }

    //Java
    //Request URL: /content/teachplan/246
    //Request Method: DELETE
    //
    //如果失败返回：
    //{"errCode":"120409","errMessage":"课程计划信息还有子级信息，无法操作"}
    //
    //如果成功：状态码200，不返回信息


    @ApiOperation("删除课程计划的章节")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTechPlan(@PathVariable("id") Long id){
        teachplanService.deleteTechPlan(id);
    }


    //Java
    //Request URL: http://localhost:8601/api/content/teachplan/movedown/43
    //Request Method: POST

    @ApiOperation("根据id减小课程计划章节的数值")
    @PostMapping("/teachplan/moveup/{id}")
    public void moveupOrderBy(@PathVariable("id") Long id){
        teachplanService.moveupOrderBy(id);
    }


//    Java
//    Request URL: http://localhost:8601/api/content/teachplan/moveup/43
//    Request Method: POST



    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }
}
