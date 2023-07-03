package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * @Author 李柯锦
 * @Date 2023/7/2 15:03
 * @Description 分页查询参数
 */

@Data
@ToString
public class PageParams {

    //当前页码
    private Long pageNo = 1L;

    //每页显示记录数
    private Long pageSize =10L;

    public PageParams(){
    }

    public PageParams(long pageNo,long pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
