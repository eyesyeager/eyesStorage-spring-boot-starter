package io.github.eyesyeager.eyesStorageStarter.service.storage;

import io.github.eyesyeager.eyesStorageStarter.aop.PutCompress;
import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import io.github.eyesyeager.eyesStorageStarter.convert.ObjectMetaDataConvert;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import io.github.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import io.github.eyesyeager.eyesStorageStarter.service.AbstractOssStorage;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.OssProperties;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.TencentProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author artonyu
 * date 2024-11-11 10:30
 */

@Slf4j
public class TencentOssStorage extends AbstractOssStorage {

    private final TencentProperties tencentProperties;

    private final COSClient cosClient;

    private final TransferManager transferManager;

    public TencentOssStorage(EyesStorageProperties.StorageContext context, OssProperties properties) {
        super(ConfigContext.SOURCE_TENCENT, context, properties);
        tencentProperties = (TencentProperties) properties;
        cosClient = createCosClient();
        transferManager = createTransferManager();
    }

    @Override
    public String getSimpleUrl(String objectName, String path) {
        return properties.getEndpoint() + (StringUtils.isEmpty(path) ? "" : "/" + path) + "/" + objectName;
    }

    @Override
    public String getSignedUrl(String objectName, String path, Long expire) throws EyesStorageException {
        Date expirationDate = new Date(System.currentTimeMillis() + expire * 1000);
        String key = buildKey(path, objectName);
        URL url = cosClient.generatePresignedUrl(properties.getBucket(), key, expirationDate, HttpMethodName.GET);
        return url.toString();
    }

    @Override
    public ObjectMetaData statObject(String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        ObjectMetadata objectMetadata = retry(() -> cosClient.getObjectMetadata(properties.getBucket(), key));
        cosClient.shutdown();
        return ObjectMetaDataConvert.tencentMetaToObjectMetaData(objectMetadata, key);
    }

    @Override
    @PutCompress(ConfigContext.SOURCE_TENCENT)
    public ObjectUploadModel putObject(byte[] data, String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        log.info("------------------------ key: {}, source: {}, do putObject start ------------------------", key, source);
        InputStream inputStream = new ByteArrayInputStream(data);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(data.length);
        PutObjectRequest putObjectRequest = new PutObjectRequest(properties.getBucket(), key, inputStream, objectMetadata);

        UploadResult result;
        try {
            result = retry(() -> {
                Upload upload = transferManager.upload(putObjectRequest);
                return upload.waitForUploadResult();
            });
        } catch (Exception e) {
            log.info("------------------------ key: {}, source: {}, do putObject fail ------------------------", key, source);
            throw new EyesStorageException(e);
        }
        log.info("------------------------ key: {}, source: {}, do putObject success ------------------------", key, source);
        return new ObjectUploadModel(result.getKey(), objectName, (long) data.length, Collections.singletonList(source));
    }

    @Override
    public ObjectUploadModel putObjectByNetUrl(String netUrl, String objectName, String path, Map<String, String> headerMap) throws EyesStorageException {
        String key = buildKey(path, objectName);
        log.info("------------------------ key: {}, source: {}, url: {}, do putObjectByNetUrl start ------------------------", key, source, netUrl);
        return null;
    }

    @Override
    public ObjectDownloadModel getObject(String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        GetObjectRequest getObjectRequest = new GetObjectRequest(properties.getBucket(), key);
        getObjectRequest.setTrafficLimit(tencentProperties.getDownloadTrafficLimit());
        COSObject cosObject = retry(() -> cosClient.getObject(getObjectRequest));
        COSObjectInputStream cosObjectInput = cosObject.getObjectContent();
        return new ObjectDownloadModel(key, objectName, cosObjectInput);
    }

    @Override
    public boolean deleteObject(String objectName, String path) throws EyesStorageException {
        String key = buildKey(path, objectName);
        return retry(() -> {
            cosClient.deleteObject(properties.getBucket(), key);
            return true;
        });
    }

    /**
     * 创建 COS 客户端
     * @return COSClient
     */
    private COSClient createCosClient() {
        COSCredentials cred = new BasicCOSCredentials(properties.getAccessKey(), properties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(tencentProperties.getRegion()));
        return new COSClient(cred, clientConfig);
    }

    /**
     * 创建 TransferManager 实例，这个实例用来后续调用高级接口
     * @return TransferManager
     */
    private TransferManager createTransferManager() {
        ExecutorService threadPool = Executors.newFixedThreadPool(tencentProperties.getTransferThread());
        return new TransferManager(cosClient, threadPool);
    }
}
