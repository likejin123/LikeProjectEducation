package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author 李柯锦
 * @Date 2023/7/4 17:20
 * @Description
 */

public class MinioTest {

    public static void main(String[] args) {
        System.out.println("abc");
    }

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    //上传文件
    @Test
    public  void upload() {
        try {

            //通过扩展名得到媒体扩展类型
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
            String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
            if(extensionMatch!=null){
                mimeType = extensionMatch.getMimeType();
            }


            //上传文件的参数信息
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")//确定桶
                    .object("001/test001.mp4")//添加子目录
                    .filename("F:\\Project_xuecheng\\test_media\\base.mp4")//指定本地目录文件
                    //F:\Project_xuecheng\test_media
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定（设置上传的媒体类型）
                    .build();
            //上传文件
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }

    //删除文件
    @Test
    public void delete(){
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket("testbucket").object("base.mp4").build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }


    //查询文件
    @Test
    public void getFile() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("test001.mp4").build();
        try(
                //拿到输入流
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);

                //输出流
                FileOutputStream outputStream = new FileOutputStream(new File("F:\\Project_xuecheng\\test_media\\1a.mp4"));
        ) {
            //流拷贝直接拷贝
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //将分块文件上传到minio
//将分块文件上传至minio
    @Test
    public void uploadChunk(){
        String chunkFolderPath = "F:\\Project_xuecheng\\test_media\\chunk";
        File chunkFolder = new File(chunkFolderPath);
        //分块文件
        File[] files = chunkFolder.listFiles();
        //将分块文件上传至minio
        for (int i = 0; i < files.length; i++) {
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs
                        .builder()
                        .bucket("testbucket")
                        .object("chunk/" + i)
                        .filename(files[i].getAbsolutePath())
                        .build();
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上传分块成功"+i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }



    //合并文件，要求分块文件最小5M
    @Test
    public void test_merge() throws Exception {
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(3)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/".concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        //指定合并后的objectname信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs
                .builder()
                .bucket("testbucket")
                .object("merge01.mp4")//合并后的文件名
                .sources(sources)//合并源文件
                .build();
        //合并文件：minIO默认分块文件是5M
        minioClient.composeObject(composeObjectArgs);

    }
    //清除分块文件
    @Test
    public void test_removeObjects(){
        //合并分块完成将分块文件清除
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(3)
                .map(i -> new DeleteObject("chunk/".concat(Integer.toString(i))))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("testbucket").objects(deleteObjects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
