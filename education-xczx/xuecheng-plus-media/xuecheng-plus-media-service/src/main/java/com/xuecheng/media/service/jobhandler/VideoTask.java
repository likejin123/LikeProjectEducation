package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
    视频处理任务
 * @author xuxueli 2019-12-11 21:52:51
 */
@Slf4j
@Component
public class VideoTask {

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;


    @Autowired
    MediaFileService mediaFileService;
    @Autowired
    MediaFileProcessService mediaFileProcessService;

    /**
     * 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();//执行器的总数

//        System.out.println("sharIndex" + shardIndex + "shardTotal" + shardTotal);


        //取出cpu核心数作为一次处理数据的条数
        int processors = Runtime.getRuntime().availableProcessors();
        //查询待处理的任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);

        //任务数量
        int size = mediaProcessList.size();
        log.debug("取到的视频处理任务书：{}",size);
        if(size <= 0){
            return;
        }

        //创建线程池。。确定cpu核心数
        ExecutorService executorService = Executors.newFixedThreadPool(size);

        //使用计数器使任务执行完成才返回（每次执行一次，size - 1）
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //开始处理任务
        mediaProcessList.forEach(mediaProcess -> {
            //将任务加入线程池
            executorService.execute(()->{
                //任务执行逻辑

                //任务表主键id
                Long taskId = mediaProcess.getId();
                //开启任务（乐观锁）
                boolean b = mediaFileProcessService.startTask(taskId);
                if(!b){
                    log.debug("任务抢占失败，任务id:{}",taskId);
                    countDownLatch.countDown();
                    return;
                }

                String bucket = mediaProcess.getBucket();
                String objectName = mediaProcess.getFilePath();
                String fileId = mediaProcess.getFileId();

                //下载minio视频到本地(bucket + objectName)
                File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                if(file == null){
                    log.debug("下载视频出错，任务id:{} ",taskId);
                    //保存任务处理失败的结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3", fileId,null,"下载视频到本地失败");
                    countDownLatch.countDown();
                    return;
                }


                //执行视频转码
                //ffmpeg的路径
                String ffmpeg_path = ffmpegpath;//ffmpeg的安装位置
                //源avi视频的路径
                String video_path = file.getAbsolutePath();
                //转换后mp4文件的名称（文件名称：md5值）
                String mp4_name = fileId + ".mp4";
                //转换后mp4文件
                File mp4TempFile = null;
                //创建临时文件作为转换后的文件
                try {
                    mp4TempFile = File.createTempFile("minio", ".mp4");
                } catch (IOException e) {
                    log.error("创建临时文件异常，任务id:{} ",taskId);
                    //保存任务处理失败的结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3", fileId,null,"创建临时文件异常");
                    e.printStackTrace();
                    countDownLatch.countDown();
                    return;
                }
                String mp4_path = mp4TempFile.getAbsolutePath();
                //创建工具类对象
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
                //开始视频转换，成功将返回success 失败返回错误信息
                String result = videoUtil.generateMp4();
                if(!result.equals("success")){
                    log.debug("视频转码失败");
                    //保存任务处理失败的结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3", mediaProcess.getFileId(),null,"视频转码失败");
                    countDownLatch.countDown();
                    return;
                }
                //新文件位置和名称
                objectName = getFilePath(fileId, ".mp4");
                //保存视频mp4到minio（新文件名称）
                boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4TempFile.getAbsolutePath(), "video/mp4",bucket, objectName);
                if(!b1){
                    log.debug("上传视频到minio失败,task:{}",taskId);
                    //保存任务处理失败的结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3", fileId,null,"上传视频到minio失败");
                    countDownLatch.countDown();
                    return;
                }

                //mp4的url
                String url = getFilePath(fileId, ".mp4");

                //保存任务的状态成功
                mediaFileProcessService.saveProcessFinishStatus(taskId,"2", fileId,url,"下载视频到本地失败");


                log.info("保存任务执行成功");

                countDownLatch.countDown();
            });
        }
        );

        //阻塞，指定最大等待限度时间
        countDownLatch.await(30, TimeUnit.MINUTES);

    }

    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

}
