package com.eyesyeager.eyesStorageStarter;

import com.eyesyeager.eyesStorageStarter.aop.PutCompressAspect;
import com.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import com.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import com.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import com.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import com.eyesyeager.eyesStorageStarter.service.storage.AliyunOssStorage;
import com.eyesyeager.eyesStorageStarter.service.storage.MinioOssStorage;
import com.eyesyeager.eyesStorageStarter.service.storage.QiniuOssStorage;
import com.eyesyeager.eyesStorageStarter.service.storage.TencentOssStorage;
import com.eyesyeager.eyesStorageStarter.starter.EyesStorageAutoConfiguration;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author artonyu
 * @date 2024-11-08 10:26
 */

@SpringBootTest(classes = {EyesStorageAutoConfiguration.class})
public class SourceStorageTest {

    @Resource
    private MinioOssStorage storage;

//    @Resource
//    private QiniuOssStorage storage;

//    @Resource
//    private TencentOssStorage storage;

//    @Resource
//    private AliyunOssStorage storage;

    @Test
    public void getSimpleUrl() {
        System.out.println(storage.getSimpleUrl("test.txt", ""));
        System.out.println(storage.getSimpleUrl("test.txt", "hh"));
        System.out.println(storage.getSimpleUrl("test.txt", "hh/nihao"));
    }

    @Test
    public void getSignedUrl() {
        try {
            String url = storage.getSignedUrl("test.txt", "", 30L);
            System.out.println(url);
        } catch (EyesStorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void statObject() {
        try {
            ObjectMetaData data = storage.statObject("test.txt", "");
            System.out.println(data);
        } catch (EyesStorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void putObject() {
        try {
            StringBuilder sb = new StringBuilder();
            int size = 10*1024*1024;
            sb.append("a".repeat(size));
            byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
            ObjectUploadModel model = storage.putObject(data, "test.txt", "");
            System.out.println(model);
        } catch (EyesStorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getObject() {
        try {
            ObjectDownloadModel model = storage.getObject("test.txt", "hhh");
            InputStream inputStream = model.getInputStream();
            File file = new File(model.getObjectName());
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedInputStream in;
            BufferedOutputStream out;
            in = new BufferedInputStream(inputStream);
            out = new BufferedOutputStream(new FileOutputStream(model.getObjectName()));
            int len = -1;
            byte[] b = new byte[1024];
            while((len = in.read(b)) != -1){
                out.write(b,0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteObject() {
        try {
            boolean b = storage.deleteObject("test.txt", "hhh");
            System.out.println(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
