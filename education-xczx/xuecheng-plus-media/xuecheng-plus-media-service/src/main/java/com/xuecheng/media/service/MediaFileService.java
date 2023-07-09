package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import io.minio.errors.*;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);



 /*
  * @Description 上传文件
  * @param companyId 机构ID
  * @param uploadFileParamsDto 文件信息
  * @param localFilePath 文件本地路径
  * @param objectNmae 供远程调用 静态页面的生成（传入objectName，按照objectname存储）
  * @return UploadFileResultDto 返回结果
  **/
 //本地文件的路径
 //文件相关信息
 //机构ID
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName);




 /*
  * @Description 代理方法调用满足事务控制
  * @param localFilePath
  * @param mimeType
  * @param bucket
  * @param objectName
  * @return boolean
  **/
 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);




 /**
  * @description 检查文件是否存在
  * @param fileMd5 文件的md5
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
  * @author Mr.M
  * @date 2022/9/13 15:38
  */
 public RestResponse<Boolean> checkFile(String fileMd5) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException;


 /**
  * @description 检查分块是否存在
  * @param fileMd5  文件的md5
  * @param chunkIndex  分块序号
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
  * @author Mr.M
  * @date 2022/9/13 15:39
  */
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);




    /**
     * @description 上传分块
     * @param fileMd5  文件md5
     * @param chunk  分块序号
     * @param localChunkFilePath 本地文件路径
     * @return com.xuecheng.base.model.RestResponse
     * @author Mr.M
     * @date 2022/9/13 15:50
     */
    public RestResponse uploadChunk(String fileMd5,int chunk,String localChunkFilePath);




    /**
     * @description 合并分块
     * @param companyId  机构id
     * @param fileMd5  文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
     * @author Mr.M
     * @date 2022/9/13 15:56
     */
    public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);


    /*
     * @Description 下载文件
     * @param bucket
     * @param objectName
     * @return File
     **/
    public File downloadFileFromMinIO(String bucket, String objectName);

    /*
     * @Description 上传文件到Minio
     * @param localFilePath 本地路径
     * @param mimeType 文件类型
     * @param bucket 桶
     * @param objectName
     * @return boolean
     **/
    public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName);

    /*
     * @Description 根据媒资id查询文件信息
     * @param mediaId
     * @return MediaFiles
     **/
    MediaFiles getFileById(String mediaId);
}
