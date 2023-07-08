package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author 李柯锦
 * @Date 2023/7/5 15:05
 * @Description
 */
public class BigFileTest {

    //分块测试
    @Test
    public void testChunk() throws Exception{
        //创建源文件
        File sourceFile = new File("F:\\Project_xuecheng\\test_media\\base.MP4");

        //分快文件存储路径
        String chunkFilePath = "F:\\Project_xuecheng\\test_media\\chunk\\";

        //分快文件大小  1M
        int chunkSize = 1024 * 1024 * 5;

        //分快文件的个数（向上取整）
        int chunkNum = (int)Math.ceil(sourceFile.length() * 1.0 / chunkSize);

        //使用流从源文件读数据，向分快文件中写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");

        //缓存区域
        byte[] bytes = new byte[1024];

        //从文件中读
        for(int i = 0 ; i < chunkNum ; i ++){
            File chunkFile = new File(chunkFilePath + i);

            //分快文件的写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;

            while((len = raf_r.read(bytes)) != -1){
                raf_rw.write(bytes,0,len);
                if(chunkFile.length() >= chunkSize){
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();

    }

    //将分块进行合并
    @Test
    public void testMegre() throws IOException {

        //创建源文件
        File sourceFile = new File("F:\\Project_xuecheng\\test_media\\base.MP4");

        //分快文件存储路径
        File chunkFolder = new File("F:\\Project_xuecheng\\test_media\\chunk\\");

        //合并文件存储在哪
        File mergeFile = new File("F:\\Project_xuecheng\\test_media\\base_2.MP4");


        //取出所有的分快文件
        File[] files = chunkFolder.listFiles();

        //将数组转成list
        List<File> fileList = Arrays.asList(files);

        //按照顺序合并文件
        Collections.sort(fileList,(File o1,File o2) ->{
            return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
        });

        //向合并文件写的流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");

        //缓冲区
        byte[] bytes = new byte[1024];

        //遍历分快文件，
        for(File file : fileList) {
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1) {
                raf_rw.write(bytes, 0, len);
            }
            raf_r.close();
        }
        raf_rw.close();

        //合并文件完成 对合并文件进行校验
        String md5_merge = DigestUtils.md5Hex(new FileInputStream(mergeFile));
        String md5_source = DigestUtils.md5Hex(new FileInputStream(sourceFile));

        if(md5_source.equals(md5_merge)){
            System.out.println("文件合并成功");
        }
    }
}
