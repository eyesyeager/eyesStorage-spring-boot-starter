package com.eyesyeager.eyesStorageStarter.service.storage;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.eyesyeager.eyesStorageStarter.aop.PutCompress;
import com.eyesyeager.eyesStorageStarter.context.ConfigContext;
import com.eyesyeager.eyesStorageStarter.convert.ObjectMetaDataConvert;
import com.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import com.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import com.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import com.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import com.eyesyeager.eyesStorageStarter.service.AbstractOssStorage;
import com.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties.StorageContext;
import com.eyesyeager.eyesStorageStarter.starter.properties.AliyunProperties;
import com.eyesyeager.eyesStorageStarter.starter.properties.OssProperties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author artonyu
 * @date 2024-11-11 10:32
 */

@Slf4j
public class AliyunOssStorage extends AbstractOssStorage {

    private final OSS ossClient;

    private final AliyunProperties aliyunProperties;

    public AliyunOssStorage(StorageContext context, OssProperties properties) {
        super(ConfigContext.SOURCE_ALIYUN, context, properties);
        aliyunProperties = (AliyunProperties) properties;
        ossClient = getOssClient();
    }

    @Override
    public String getSimpleUrl(String objectName, String path) {
        String httpDelimiter = "://";
        String[] split = properties.getEndpoint().split(httpDelimiter);
        return split[0] + httpDelimiter + properties.getBucket() + "." +
                split[1] + (StringUtils.isEmpty(path) ? "" : "/" + path) + "/" + objectName;
    }

    @Override
    public String getSignedUrl(String objectName, String path, Long expire) throws EyesStorageException {
        Date expiration = new Date(new Date().getTime() + expire * 1000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(properties.getBucket(), objectName, HttpMethod.GET);
        request.setExpiration(expiration);
        URL url = retry(() -> ossClient.generatePresignedUrl(request));
        return url.toString();
    }

    @Override
    public ObjectMetaData statObject(String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        ObjectMetadata metadata = retry(() -> ossClient.getObjectMetadata(properties.getBucket(), key));
        return ObjectMetaDataConvert.aliyunMetaToObjectMetaData(metadata, key);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_ALIYUN)
    public ObjectUploadModel putObject(byte[] data, String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        log.info("------------------------ key: {}, source: {}, do putObject start ------------------------", key, source);
        try {
            InputStream inputStream = new ByteArrayInputStream(data);
            PutObjectRequest putObjectRequest = new PutObjectRequest(properties.getBucket(), key, inputStream);
            retry(() -> ossClient.putObject(putObjectRequest));
        } catch (Exception e) {
            log.info("------------------------ key: {}, source: {}, do putObject fail ------------------------", key, source);
            throw new EyesStorageException(e);
        }
        log.info("------------------------ key: {}, source: {}, do putObject success ------------------------", key, source);
        return new ObjectUploadModel(key, objectName, (long) data.length, Collections.singletonList(source));
    }

    @Override
    public ObjectDownloadModel getObject(String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        OSSObject object = retry(() -> ossClient.getObject(properties.getBucket(), key));
        return new ObjectDownloadModel(object.getKey(), objectName, object.getObjectContent());
    }

    @Override
    public boolean deleteObject(String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        retry(() -> ossClient.deleteObject(properties.getBucket(), key));
        return true;
    }

    /**
     * 获取oss客户端
     * @return OSS
     */
    private OSS getOssClient() {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(properties.getAccessKey(), properties.getSecretKey());
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        return OSSClientBuilder.create()
                .endpoint(properties.getEndpoint())
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(aliyunProperties.getRegion())
                .build();
    }
}
