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
import com.eyesyeager.eyesStorageStarter.starter.properties.QiniuProperties;
import com.eyesyeager.eyesStorageStarter.utils.QiniuRegionUtils;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import java.io.InputStream;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * @author artonyu
 * @date 2024-11-08 10:24
 */

@Slf4j
public class QiniuOssStorage extends AbstractOssStorage {

    private final QiniuProperties qiniuProperties;

    public QiniuOssStorage(StorageContext context, QiniuProperties properties) {
        super(ConfigContext.SOURCE_QINIU, context, properties);
        this.qiniuProperties = properties;
    }

    @Override
    public String getSimpleUrl(String objectName, String path) {
        return properties.getEndpoint() + (StringUtils.isEmpty(path) ? "" : "/" + path) + "/" + objectName;
    }

    @Override
    public String getSignedUrl(String objectName, String path, Long expireTime) throws EyesStorageException {
        try {
            String baseUrl = getSimpleUrl(objectName, path);
            Auth auth = Auth.create(properties.getAccessKey(), properties.getSecretKey());
            return auth.privateDownloadUrl(baseUrl, expireTime);
        } catch (Exception e) {
            throw new EyesStorageException(e);
        }
    }

    @Override
    public ObjectMetaData statObject(String objectName, String path) throws EyesStorageException {
        Region region = QiniuRegionUtils.getRegion(qiniuProperties.getRegion());
        Configuration cfg = new Configuration(region);
        Auth auth = Auth.create(properties.getAccessKey(), properties.getSecretKey());
        BucketManager bucketManager = new BucketManager(auth, cfg);

        String key = buildKey(path, objectName);
        FileInfo fileInfo = retry(() -> bucketManager.stat(properties.getBucket(), key));
        return ObjectMetaDataConvert.qiniuFileInfoToObjectMetaData(fileInfo, key);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_QINIU)
    public ObjectUploadModel putObject(byte[] data, String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        log.info("------------------------ key: {}, source: {}, do putObject start ------------------------", key, source);
        try {
            Region region = QiniuRegionUtils.getRegion(qiniuProperties.getRegion());
            Configuration cfg = new Configuration(region);
            cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
            UploadManager uploadManager = new UploadManager(cfg);

            InputStream inputStream = new ByteArrayInputStream(data);
            Auth auth = Auth.create(properties.getAccessKey(), properties.getSecretKey());
            String upToken = auth.uploadToken(properties.getBucket());
            retry(() -> uploadManager.put(inputStream, key, upToken, null, null));
        } catch (Exception e) {
            log.info("------------------------ key: {}, source: {}, do putObject fail ------------------------", key, source);
            throw new EyesStorageException(e);
        }
        log.info("------------------------ key: {}, source: {}, do putObject success ------------------------", key, source);
        return new ObjectUploadModel(key, objectName, (long)data.length, Collections.singletonList(source));
    }

    @Override
    public ObjectDownloadModel getObject(String objectName, String path) throws EyesStorageException {
        throw new EyesStorageException("QiniuOssStorage does not support getObject");
    }

    @Override
    public boolean deleteObject(String objectName, String path) throws EyesStorageException {
        Region region = QiniuRegionUtils.getRegion(qiniuProperties.getRegion());
        Configuration cfg = new Configuration(region);
        Auth auth = Auth.create(properties.getAccessKey(), properties.getSecretKey());
        BucketManager bucketManager = new BucketManager(auth, cfg);

        String key = buildKey(path, objectName);
        retry(() -> bucketManager.delete(properties.getBucket(), key));
        return true;
    }
}
