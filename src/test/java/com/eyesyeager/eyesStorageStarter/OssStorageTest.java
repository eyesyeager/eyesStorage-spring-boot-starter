package com.eyesyeager.eyesStorageStarter;

import com.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import com.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import com.eyesyeager.eyesStorageStarter.service.EyesOssStorage;
import com.eyesyeager.eyesStorageStarter.starter.EyesStorageAutoConfiguration;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author artonyu
 * @date 2024-11-08 10:26
 */

@SpringBootTest(classes = EyesStorageAutoConfiguration.class)
public class OssStorageTest {

    @Resource
    private EyesOssStorage storage;

    @Test
    public void putObject() {
        try {
            byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
            ObjectUploadModel model = storage.putObject(data, "test.txt", "ooo");
            System.out.println(model);
        } catch (EyesStorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteObject() {
        try {
            boolean b = storage.deleteObject("test.txt", "ooo");
            System.out.println(b);
        } catch (EyesStorageException e) {
            e.printStackTrace();
        }
    }
}
