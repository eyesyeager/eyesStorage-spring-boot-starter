package com.eyesyeager.eyesStorageStarter.convert;

import com.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import com.eyesyeager.eyesStorageStarter.utils.DateUtils;
import com.qiniu.storage.model.FileInfo;
import io.minio.StatObjectResponse;
import java.time.ZonedDateTime;

/**
 * @author artonyu
 * @date 2024-11-08 10:28
 */

public class ObjectMetaDataConvert {
    /**
     * StatObjectResponse 转换为 ObjectMetaData
     * @param response StatObjectResponse
     * @return ObjectInfo
     */
    public static ObjectMetaData minioStatToObjectMetaData(StatObjectResponse response) {
        ObjectMetaData objectMetaData = new ObjectMetaData();
        objectMetaData.setKey(response.object());
        objectMetaData.setSize(response.size());
        objectMetaData.setContentType(response.contentType());
        objectMetaData.setLastModified(response.lastModified());
        objectMetaData.setVersionId(response.versionId());
        objectMetaData.setEtag(response.etag());
        objectMetaData.setIsDelete(response.deleteMarker());
        objectMetaData.setUserMetadata(response.userMetadata());
        return objectMetaData;
    }

    /**
     * FileInfo 转换为 ObjectMetaData
     * @param fileInfo FileInfo
     * @param key path + objectName
     * @return ObjectMetaData
     */
    public static ObjectMetaData qiniuFileInfoToObjectMetaData(FileInfo fileInfo, String key) {
        ObjectMetaData objectMetaData = new ObjectMetaData();
        objectMetaData.setKey(key);
        objectMetaData.setSize(fileInfo.fsize);
        objectMetaData.setContentType(fileInfo.mimeType);
        ZonedDateTime lastModified = DateUtils.timestampToZonedDateTime(fileInfo.putTime);
        objectMetaData.setLastModified(lastModified);
        objectMetaData.setEtag(fileInfo.md5);
        objectMetaData.setIsDelete(fileInfo.status == 1);
        objectMetaData.setUserMetadata(fileInfo.meta);
        return objectMetaData;
    }

    /**
     * com.qcloud.cos.model.ObjectMetadata 转换为 ObjectMetaData
     * @param metaData com.qcloud.cos.model.ObjectMetadata
     * @param key key
     * @return ObjectMetaData
     */
    public static ObjectMetaData tencentMetaToObjectMetaData(com.qcloud.cos.model.ObjectMetadata metaData, String key) {
        ObjectMetaData objectMetaData = new ObjectMetaData();
        objectMetaData.setKey(key);
        objectMetaData.setSize(metaData.getContentLength());
        objectMetaData.setContentType(metaData.getContentType());
        ZonedDateTime lastModified = DateUtils.dateToZonedDateTime(metaData.getLastModified());
        objectMetaData.setLastModified(lastModified);
        objectMetaData.setVersionId(metaData.getVersionId());
        objectMetaData.setEtag(metaData.getETag());
        objectMetaData.setIsDelete(metaData.isDeleteMarker());
        objectMetaData.setUserMetadata(metaData.getUserMetadata());
        return objectMetaData;
    }

    /**
     * com.aliyun.oss.model.ObjectMetadata 转换为 ObjectMetaData
     * @param metaData com.aliyun.oss.model.ObjectMetadata
     * @return ObjectMetaData
     */
    public static ObjectMetaData aliyunMetaToObjectMetaData(com.aliyun.oss.model.ObjectMetadata metaData, String key) {
        ObjectMetaData objectMetaData = new ObjectMetaData();
        objectMetaData.setKey(key);
        objectMetaData.setSize(metaData.getContentLength());
        objectMetaData.setContentType(metaData.getContentType());
        ZonedDateTime lastModified = DateUtils.dateToZonedDateTime(metaData.getLastModified());
        objectMetaData.setLastModified(lastModified);
        objectMetaData.setVersionId(metaData.getVersionId());
        objectMetaData.setEtag(metaData.getETag());
        objectMetaData.setUserMetadata(metaData.getUserMetadata());
        return objectMetaData;
    }
}
