package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */

@Slf4j
 @Service
public class MediaFileServiceImpl implements MediaFileService {

  @Autowired
 MediaFilesMapper mediaFilesMapper;
  @Autowired
 MinioClient minioClient;

  //代理对象。。用于事务控制
  @Autowired
  MediaFileService mediaFileService;

  @Autowired
  MediaProcessMapper mediaProcessMapper;

 //存储普通文件普通文件桶
 @Value("${minio.bucket.files}")
 private String bucket_Files;

 //video文件桶
 @Value("${minio.bucket.videofiles}")
 private String bucket_video;


 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  
  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }



    /*
  * @Description 上传图片接口（保存MinIO 保存数据库）
  * @param companyId
 * @param uploadFileParamsDto
 * @param localFilePath 本地存储的文件绝对路径
 * @param objectName 存储html的路径（供远程调用，让html放到course目录下，而不是放到年月日）
  * @return UploadFileResultDto
  **/
 @Transactional
 @Override
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath, String objectName) {

  //上传文件到minio

  //先得到扩展名
  String filename = uploadFileParamsDto.getFilename();
  String extention = filename.substring(filename.lastIndexOf("."));

  //将文件上传到minIO
  String mimeType = getMimeType(extention);

  //保存文件的相对地址
  String defaultFolderPath = getDefaultFolderPath();

  //文件的md5值（文件名字）
  String fileMd5 = getFileMd5(new File(localFilePath));

  if(StringUtil.isEmpty(objectName)){
      objectName = defaultFolderPath + fileMd5 + extention;
  }

  //以年月日生成
  boolean result = addMediaFilesToMinIO(localFilePath,mimeType,bucket_Files,objectName);
  if(!result){
   XueChengPlusException.cast("上传文件失败");
  }


  //将信息保存到数据库

  //判断是否重复上传

   MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_Files, objectName);

  if(mediaFiles == null){
   XueChengPlusException.cast("文件上传后保存信息失败");
  }
  //准备返回的对象

  UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
  BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);

  return uploadFileResultDto;
 }

 /*
  * @Description 根据扩展名获取MimeType
  * @param extension
  * @return String
  **/
 private String getMimeType(String extension){
  if(extension==null)
   extension = "";
  //根据扩展名取出mimeType
  ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
  //通用mimeType，字节流
  String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
  if(extensionMatch!=null){
   mimeType = extensionMatch.getMimeType();
  }
  return mimeType;
 }

 /*
  * @Description 上传文件到Minio
  * @param localFilePath 本地路径
  * @param mimeType 文件类型
  * @param bucket 桶
  * @param objectName
  * @return boolean
  **/
 public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName) {
  try {
   UploadObjectArgs testbucket = UploadObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .filename(localFilePath)
           .contentType(mimeType)
           .build();
   minioClient.uploadObject(testbucket);
   log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
   System.out.println("上传成功");
   return true;
  } catch (Exception e) {
   e.printStackTrace();
   log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
   XueChengPlusException.cast("上传文件到文件系统失败");
  }
  return false;
 }

    @Override
    public MediaFiles getFileById(String mediaId) {

        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        return mediaFiles;
    }

    /*
  * @Description 根据当前时间获取保存文件的路径年/月/日
  * @param
  * @return String
  **/
 //获取文件默认存储目录路径 年/月/日
 private String getDefaultFolderPath() {
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  String folder = sdf.format(new Date()).replace("-", "/")+"/";
  return folder;
 }


 /*
  * @Description 获取文件的md5值
  * @param file
  * @return String
  **/
 private String getFileMd5(File file) {
  try (FileInputStream fileInputStream = new FileInputStream(file)) {
   String fileMd5 = DigestUtils.md5Hex(fileInputStream);
   return fileMd5;
  } catch (Exception e) {
   e.printStackTrace();
   return null;
  }
 }


 /**
  * @description 将文件信息保存到数据库（同时将文件信息保存到待处理任务 视频转码）
  * @param companyId  机构id
  * @param fileMd5  文件md5值
  * @param uploadFileParamsDto  上传文件的信息
  * @param bucket  桶
  * @param objectName 对象名称
  * @return com.xuecheng.media.model.po.MediaFiles
  * @author Mr.M
  * @date 2022/10/12 21:22
  */
 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
    //从数据库查询文件
    MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
    if (mediaFiles == null) {
     mediaFiles = new MediaFiles();
     //拷贝基本信息
     BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
     mediaFiles.setId(fileMd5);
     mediaFiles.setFileId(fileMd5);
     mediaFiles.setCompanyId(companyId);
     mediaFiles.setUrl("/" + bucket + "/" + objectName);
     mediaFiles.setBucket(bucket);
     mediaFiles.setFilePath(objectName);
     mediaFiles.setCreateDate(LocalDateTime.now());
     mediaFiles.setAuditStatus("002003");
     mediaFiles.setStatus("1");
     //保存文件信息到文件表
     int insert = mediaFilesMapper.insert(mediaFiles);
     if (insert < 0) {
      log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
      XueChengPlusException.cast("保存文件信息失败");
     }
     log.debug("保存文件信息到数据库成功--------------------------------,{}",mediaFiles.toString());

    }

    //记录待处理任务。
     //通过mimeType判断如果是avi视频才写入待处理任务。
    addWaitingTask(mediaFiles);

     // 向media_process表中插入记录。
    return mediaFiles;

 }


    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        //获取文件的mimeType
        String filename = mediaFiles.getFilename();

        //文件名称
        String extention = filename.substring(filename.lastIndexOf("."));

        //获取媒体类型
        String mimeType = getMimeType(extention);

        if(mimeType.equals("video/x-msvideo")){
            //通过mimeType判断如果是avi视频才写入待处理任务。
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);

            //状态
            mediaProcess.setStatus("1");//未处理

            mediaProcess.setCreateDate(LocalDateTime.now());

            mediaProcess.setFailCount(0);//失败次数默认为0

            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }


        // 向media_process表中插入记录。
    }




 /*
  * @Description 检查文件是否存在（大视频）
  * @param fileMd5
  * @return RestResponse<Boolean>
  **/
 @Override
 public RestResponse<Boolean> checkFile(String fileMd5) {
  //先查询数据库
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if(mediaFiles != null){
      //获取桶
      String bucket = mediaFiles.getBucket();

      //objectName
      String filePath = mediaFiles.getFilePath();

      GetObjectArgs getObjectArgs = GetObjectArgs
           .builder()
           .bucket(bucket)
           .object(filePath)
              .build();
           //拿到输入流
      try{
          FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
          if(inputStream != null){
              //文件已经存在
              return RestResponse.success(true);
          }
      }catch (Exception e){
          e.printStackTrace();
      }


  }


     //文件不存在
     return RestResponse.success(false);
 }

 /*
  * @Description 检查分块（大视频）
  * @param fileMd5
  * @param chunkIndex
  * @return RestResponse<Boolean>
  **/
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

        //分块的存储路径：md5前两位为两个子目录，chunk存储分块文件
        //根据md5得到分块文件的存储路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);



        GetObjectArgs getObjectArgs = GetObjectArgs
                .builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        //拿到输入流
        try{
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if(inputStream != null){
                //文件已经存在
                return RestResponse.success(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //文件不存在
        return RestResponse.success(false);
    }

    /*
     * @Description 上传分块（大视频）
     * @param fileMd5
     * @param chunk
     * @param localChunkFilePath
     * @return RestResponse
     **/
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {


        //分块文件的路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        //定义MimeType
        String mimeType = getMimeType(null);
        //将分块文件上传到minio
        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFilePath);

        if(!b){
            return RestResponse.validfail(false,"上传分块文件失败");
        }

        return RestResponse.success(true);
    }

    /*
     * @Description 合并文件
     * @param companyId
     * @param fileMd5
     * @param chunkTotal
     * @param uploadFileParamsDto
     * @return RestResponse
     **/
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {

        //---------------------------合并文件
        //分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        //找到所有的分块文件 调用minio的sdk进行文件合并
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFileFolderPath + i)
                        .build())
                .collect(Collectors.toList());


        //源文件名称
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extention = filename.substring(filename.lastIndexOf("."));

        //合并后文件的objectname
        String objectName = getFilePathByMd5(fileMd5, extention);
        //指定合并后的objectname信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs
                .builder()
                .bucket(bucket_video)
                .object(objectName)//合并后的文件名名称
                .sources(sources)//合并源文件
                .build();
        //合并文件：minIO默认分块文件是5M
        try{
            minioClient.composeObject(composeObjectArgs);
        }catch(Exception e){
            e.printStackTrace();
            log.error("合并文件出错，bucket:{},objectName:{},错误信息：{}",bucket_video,objectName,e.getMessage());
            return RestResponse.validfail(false,"合并文件异常");
        }

        log.info("合并文件成功----------------------------------");

        //---------------------------校验文件
        //校验合并后的文件与源文件是否一致  视频上传成功
        //下载文件
        File file = downloadFileFromMinIO(bucket_video, objectName);
        //计算合并后文件的md5
        try(FileInputStream inputStream = new FileInputStream(file)){
            String mergeFile_md5 = DigestUtils.md5Hex(inputStream);
            //比较原始md5值和合并后的md5值
            if(!fileMd5.equals(mergeFile_md5)){
                log.error("校验合并文件md5值不一致，原始文件：{}，合并文件：{}",fileMd5,mergeFile_md5);
                return RestResponse.validfail(false,"文件校验失败");
            }
            //封装数据库参数文件大小
            uploadFileParamsDto.setFileSize(file.length());
        }catch (Exception e){
            return RestResponse.validfail(false,"文件校验失败");
        }
        log.info("校验文件成功----------------------------------");

        //---------------------------信息入库
        //文件信息入库
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if(mediaFiles == null){
            log.error("文件入库失败");
            return RestResponse.validfail(false,"文件入库失败");
        }

        //---------------------------清理分块
        //清理分块文件
        //拿到分块文件的路径

        clearChunkFiles(chunkFileFolderPath,chunkTotal);

        return RestResponse.success(true);
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }


    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }
    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());


            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").objects(deleteObjects).build();


            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            //非常容易超时。。。
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                    log.info("--------------------------------------------------------------删除了1个块");
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }

}
