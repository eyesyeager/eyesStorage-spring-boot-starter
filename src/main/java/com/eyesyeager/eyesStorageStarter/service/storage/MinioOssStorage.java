package com.eyesyeager.eyesStorageStarter.service.storage;

import com.eyesyeager.eyesStorageStarter.aop.PutCompress;
import com.eyesyeager.eyesStorageStarter.context.ConfigContext;
import com.eyesyeager.eyesStorageStarter.convert.ObjectMetaDataConvert;
import com.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import com.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import com.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import com.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import com.eyesyeager.eyesStorageStarter.service.AbstractOssStorage;
import com.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties.StorageContext;
import com.eyesyeager.eyesStorageStarter.starter.properties.MinioProperties;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author artonyu
 * @date 2024-11-08 10:24
 */

@Slf4j
public class MinioOssStorage extends AbstractOssStorage {

    private final MinioClient minioClient;

    public MinioOssStorage(StorageContext context, MinioProperties properties) {
        super(ConfigContext.SOURCE_MINIO, context, properties);
        this.minioClient = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    @Override
    public String getSimpleUrl(String objectName, String path) {
        return properties.getEndpoint() + "/" + properties.getBucket() +
                (StringUtils.isEmpty(path) ? "" : "/" + path) + "/" + objectName;
    }

    @Override
    public String getSignedUrl(String objectName, String path, Long expireTime) throws EyesStorageException {
        try {
            String key = buildKey(path, objectName);
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(properties.getBucket())
                    .object(key)
                    .expiry(Math.toIntExact(expireTime))
                    .method(Method.GET)
                    .build());
        } catch (Exception e) {
            throw new EyesStorageException(e);
        }
    }

    @Override
    public ObjectMetaData statObject(String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        StatObjectResponse response = retry(() -> minioClient.statObject(StatObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(key)
                .build()));
        return ObjectMetaDataConvert.minioStatToObjectMetaData(response);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_MINIO)
    public ObjectUploadModel putObject(byte[] data, String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        log.info("------------------------ key: {}, source: {}, do putObject start ------------------------", key, source);
        try {
            InputStream inputStream = new ByteArrayInputStream(data);
            retry(() -> minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(key)
                    .stream(inputStream, data.length, -1)
                    .build()));
        } catch (Exception e) {
            log.info("------------------------ key: {}, source: {}, do putObject fail ------------------------", key, source);
            throw new EyesStorageException(e);
        }
        log.info("------------------------ key: {}, source: {}, do putObject success ------------------------", key, source);
        return new ObjectUploadModel(key, objectName, (long)data.length, Collections.singletonList(source));
    }

    @Override
    public ObjectDownloadModel getObject(String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        GetObjectResponse response = retry(() -> minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(key)
                .build()));
        return new ObjectDownloadModel(key, objectName, response);
    }

    @Override
    public boolean deleteObject(String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        return retry(() -> {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(key)
                    .build());
            return true;
        });
    }
}
