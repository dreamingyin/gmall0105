package com.atguigu.gmall.utils;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by Steven on 2019/12/23.
 */

/**
 * FastDFs文件上传工具类
 */
public class PmsUploadUtils {


    public static String uploadImage(MultipartFile multipartFile) throws IOException {
        String urlImage="http://47.101.36.177";
        //1.获得配置文件的路径
        String filePath=new ClassPathResource("trac.conf").getFile().getAbsolutePath();
        try {
            ClientGlobal.init(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer,null);
        try {
            //获取文件后缀名
            byte[] bytes = multipartFile.getBytes();//获得文件上传的二进制对象
            //获取文件名称
            String originalFilename = multipartFile.getOriginalFilename();
            System.out.println(originalFilename);
            int last=originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(last);
            String[] uploadInfo = storageClient.upload_appender_file(bytes, extName,null);
            for (String s : uploadInfo) {
                urlImage+="/"+s;
            }

        } catch (MyException e) {
            e.printStackTrace();
        }
        return urlImage;
    }
}
