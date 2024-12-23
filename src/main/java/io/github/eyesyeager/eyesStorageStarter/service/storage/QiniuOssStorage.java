package io.github.eyesyeager.eyesStorageStarter.service.storage;

import io.github.eyesyeager.eyesStorageStarter.aop.PutCompress;
import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import io.github.eyesyeager.eyesStorageStarter.convert.ObjectMetaDataConvert;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import io.github.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import io.github.eyesyeager.eyesStorageStarter.service.AbstractOssStorage;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.QiniuProperties;
import io.github.eyesyeager.eyesStorageStarter.utils.QiniuRegionUtils;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import java.io.InputStream;
import java.util.Collections;
import io.github.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties;
import java.io.ByteArrayInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * @author artonyu
 * date 2024-11-08 10:24
 */

public class QiniuOssStorage extends AbstractOssStorage {

    private final QiniuProperties qiniuProperties;

    public QiniuOssStorage(EyesStorageProperties.StorageContext context, QiniuProperties properties) {
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
        InputStream inputStream = new ByteArrayInputStream(data);
        return putObject(inputStream, objectName, path, (long) data.length);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_QINIU)
    public ObjectUploadModel putObject(InputStream is, String objectName, String path) throws EyesStorageException {
        return putObject(is, objectName, path, null);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_QINIU)
    public ObjectUploadModel putObject(InputStream is, String objectName, String path, Long length) throws EyesStorageException {
                Region region = QiniuRegionUtils.getRegion(qiniuProperties.getRegion());
        Configuration cfg = new Configuration(region);
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(properties.getAccessKey(), properties.getSecretKey());
        String upToken = auth.uploadToken(properties.getBucket());
        String key = buildKey(path, objectName);
        retry(() -> uploadManager.put(is, key, upToken, null, null));
        return new ObjectUploadModel(key, objectName, Collections.singletonList(source));
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
