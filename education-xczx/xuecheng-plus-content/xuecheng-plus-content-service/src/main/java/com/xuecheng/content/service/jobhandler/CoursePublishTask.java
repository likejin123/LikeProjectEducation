package com.xuecheng.content.service.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author 李柯锦
 * @Date 2023/7/7 20:52
 * @Description 课程发布任务类
 */

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {


    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }


    /*
     * @Description 执行课程发布的任务逻辑方法
     * @param mqMessage
     * @return boolean
     **/
    @Override
    public boolean execute(MqMessage mqMessage) {

        //向mqmessage中获取课程id
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        //课程静态化上传到minio
        generateCourseHtml(mqMessage,courseId);

        //向es写索引数据
        saveCourseIndex(mqMessage,courseId);

        //向redis写缓存
        saveCourseCache(mqMessage,courseId);


        //返回true表示任务完成
        return true;
    }


    //生成课程静态化页面并上传至文件系统
    public void generateCourseHtml(MqMessage mqMessage,long courseId){

        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();

        //消息幂等性处理

        //查询数据库取出该阶段的执行状态（如果设置为1  则完成）
        int stageOne = mqMessageService.getStageOne(id);
        if(stageOne >0){
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }
        //TODO 开始进行课程静态化
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //保存第一阶段状态（保存该任务的字段为stage_one 为1）
        mqMessageService.completedStageOne(id);

    }

    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        log.debug("将课程信息缓存至redis,课程id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage,long courseId){

        //任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        //取出第二个状态的索引
        int stageTwo = mqMessageService.getStageTwo(taskId);
        //任务幂等性判断
        if(stageTwo > 0){
            log.debug("课程索引信息已经写入，无序执行");
            return;
        }


        //TODO 查询课程信息，调用搜索服务添加索引

        //完成第二阶段的任务（保存该任务的字段为stage_two 为1）
        mqMessageService.completedStageTwo(taskId);
        log.debug("保存课程索引信息,课程id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
