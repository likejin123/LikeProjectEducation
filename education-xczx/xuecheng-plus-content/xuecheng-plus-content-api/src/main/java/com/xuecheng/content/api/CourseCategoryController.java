package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author 李柯锦
 * @Date 2023/7/2 21:30
 * @Description 课程分类接口
 */

@Api(tags = "课程分类接口")
@RestController
public class CourseCategoryController {


    @Autowired
    CourseCategoryService courseCategoryService;

    @ApiOperation("课程分类树形信息查询")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }
}
