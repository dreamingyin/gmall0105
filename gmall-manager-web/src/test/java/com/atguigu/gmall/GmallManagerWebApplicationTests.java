package com.atguigu.gmall;


import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerWebApplicationTests {

	@Test
	public void contextLoads() throws IOException, MyException {
        //1.获得配置文件的路径
        String filePath=new ClassPathResource("trac.conf").getFile().getAbsolutePath();
        ClientGlobal.init(filePath);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer,null);
        String url="http://47.101.36.177";

        String[] uploadInfo = storageClient.upload_appender_file("E:/NodeProject/girl.png", "png",null);
        for (String s : uploadInfo) {
            url+="/"+s;
        }
        System.out.println("图片地址："+url);
    }

}
