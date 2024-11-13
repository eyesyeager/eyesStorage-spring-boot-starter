package com.eyesyeager.eyesStorageStarter.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

/**
 * 压缩工具类
 * @author artonyu
 * @date 2024-11-13 11:37
 */

public class CompressUtils {

    private static final String COMPRESS_FORMAT = ".zip";

    /**
     * 压缩文件
     * @param data
     * @return
     */
    public static byte[] compressObject(byte[] data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 获取压缩后的文件名
     * @param objectName
     * @return
     */
    public static String getCompressObjectName(String objectName) {
        return objectName + COMPRESS_FORMAT;
    }
}
