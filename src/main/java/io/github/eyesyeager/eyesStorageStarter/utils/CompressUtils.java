package io.github.eyesyeager.eyesStorageStarter.utils;

import java.io.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

/**
 * 压缩工具类
 * @author artonyu
 * date 2024-11-13 11:37
 */

public class CompressUtils {

    private static final String COMPRESS_FORMAT = ".gz";

    /**
     * 压缩文件
     * @param data 待压缩数据
     * @return 压缩后的数据
     */
    public static byte[] compressObject(byte[] data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 压缩文件
     * @param is 待压缩数据
     * @return 压缩后的数据
     */
    public static InputStream compressObject(InputStream is) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(byteArrayOutputStream)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                gzipOutputStream.write(buffer, 0, len);
            }
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    /**
     * 获取压缩后的文件名
     * @param objectName 原始文件名
     * @return 压缩后的文件名
     */
    public static String getCompressObjectName(String objectName) {
        return objectName + COMPRESS_FORMAT;
    }
}
