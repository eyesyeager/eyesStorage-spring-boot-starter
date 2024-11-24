package io.github.eyesyeager.eyesStorageStarter.service.storage;

import io.github.eyesyeager.eyesStorageStarter.aop.PutCompress;
import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import io.github.eyesyeager.eyesStorageStarter.convert.ObjectMetaDataConvert;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import io.github.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import io.github.eyesyeager.eyesStorageStarter.service.AbstractOssStorage;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.MinioProperties;
import io.github.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties;
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
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * @author artonyu
 * date 2024-11-08 10:24
 */

public class MinioOssStorage extends AbstractOssStorage {

    private final MinioClient minioClient;

    public MinioOssStorage(EyesStorageProperties.StorageContext context, MinioProperties properties) {
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
        InputStream inputStream = new ByteArrayInputStream(data);
        return putObject(inputStream, objectName, path, (long) data.length);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_MINIO)
    public ObjectUploadModel putObject(InputStream is, String objectName, String path) throws EyesStorageException {
        return putObject(is, objectName, path, null);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_MINIO)
    public ObjectUploadModel putObject(InputStream is, String objectName, String path, Long length) throws EyesStorageException {
        String key = buildKey(path, objectName);
        retry(() -> {
            long objectSize = -1;
            long partSize = ConfigContext.MINIO_UPLOAD_PART_SIZE;
            if (Objects.nonNull(length)) {
                objectSize = length;
                partSize = -1;
            }
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(key)
                    .stream(is, objectSize, partSize)
                    .build());
        });
        return new ObjectUploadModel(key, objectName, Collections.singletonList(source));
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
