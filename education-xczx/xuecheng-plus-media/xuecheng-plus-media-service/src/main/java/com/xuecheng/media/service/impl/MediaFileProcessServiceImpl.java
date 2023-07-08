package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author 李柯锦
 * @Date 2023/7/7 10:22
 * @Description
 */


@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {


    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;


    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }





    //实现如下
    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result<=0?false:true;
    }


    /*
     * @Description 更新任务处理结果 （media_process任务处理表）
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param errorMsg
     * @return void
     **/
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {

        //要更新的处理任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return;
        }

        //更新看状态
        //如果任务执行失败
        if(status.equals("3")){
            //更新media_process表的状态
            mediaProcess.setStatus("3");
            //失败次数加以
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);

            //更新表
            mediaProcessMapper.updateById(mediaProcess);

            //高效的更新方式

            return;
        }



        //如果任务执行成功
        //任务处理成功

        //更新mediaFile中的url（视频可以访问地址）
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if(mediaFiles!=null){
            //更新媒资文件中的访问url
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }

        //处理成功，更新media_process的url和状态
        mediaProcess.setUrl(url);
        //更新状态为2 处理成功
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        //将media_process的记录插入到media_process_history添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);


        //删除mediaProcess中的表
        mediaProcessMapper.deleteById(mediaProcess.getId());





    }

}
