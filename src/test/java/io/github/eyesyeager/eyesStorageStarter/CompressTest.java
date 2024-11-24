package io.github.eyesyeager.eyesStorageStarter;

import io.github.eyesyeager.eyesStorageStarter.utils.CompressUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件压缩测试
 * @author artonyu
 * date 2024-11-24 13:30
 */
public class CompressTest {

    @Test
    public void testCompressBytes() throws Exception {
        byte[] data = prepareData();
        byte[] bytes = CompressUtils.compressObject(data);
        writeObject(new ByteArrayInputStream(bytes));
    }

    @Test
    public void testCompressIS() throws Exception {
        byte[] data = prepareData();
        InputStream is = CompressUtils.compressObject(new ByteArrayInputStream(data));
        writeObject(is);
    }

    private static byte[] prepareData() {
        StringBuilder sb = new StringBuilder();
        int size = 10*1024*1024;
        for (int i = 0; i < size; i++) {
            sb.append("a");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void writeObject(InputStream is) throws IOException {
        String objectName = "compress.txt.gz";
        File file = new File(objectName);
        if(!file.exists()) {
            file.createNewFile();
        }
        BufferedInputStream in;
        BufferedOutputStream out;
        in = new BufferedInputStream(is);
        out = new BufferedOutputStream(Files.newOutputStream(Paths.get(objectName)));
        int len = -1;
        byte[] b = new byte[10 * 1024];
        while((len = in.read(b)) != -1){
            out.write(b,0, len);
        }
        in.close();
        out.close();
    }
}
