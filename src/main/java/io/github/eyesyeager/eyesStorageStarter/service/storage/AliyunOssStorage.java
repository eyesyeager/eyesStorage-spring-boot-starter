package io.github.eyesyeager.eyesStorageStarter.service.storage;

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
import io.github.eyesyeager.eyesStorageStarter.aop.PutCompress;
import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import io.github.eyesyeager.eyesStorageStarter.convert.ObjectMetaDataConvert;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import io.github.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import io.github.eyesyeager.eyesStorageStarter.service.AbstractOssStorage;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.AliyunProperties;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.OssProperties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import io.github.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties;
import org.apache.commons.lang3.StringUtils;

/**
 * @author artonyu
 * date 2024-11-11 10:32
 */

public class AliyunOssStorage extends AbstractOssStorage {

    private final OSS ossClient;

    private final AliyunProperties aliyunProperties;

    public AliyunOssStorage(EyesStorageProperties.StorageContext context, OssProperties properties) {
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
        InputStream inputStream = new ByteArrayInputStream(data);
        return putObject(inputStream, objectName, path, (long) data.length);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_ALIYUN)
    public ObjectUploadModel putObject(InputStream is, String objectName, String path) throws EyesStorageException {
        return putObject(is, objectName, path, null);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_ALIYUN)
    public ObjectUploadModel putObject(InputStream is, String objectName, String path, Long length) throws EyesStorageException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        if (Objects.nonNull(length)) {
            objectMetadata.setContentLength(length);
        }
        String key = buildKey(path, objectName);
        PutObjectRequest putObjectRequest = new PutObjectRequest(properties.getBucket(), key, is, objectMetadata);
        retry(() -> ossClient.putObject(putObjectRequest));
        return new ObjectUploadModel(key, objectName, Collections.singletonList(source));
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
     *
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
