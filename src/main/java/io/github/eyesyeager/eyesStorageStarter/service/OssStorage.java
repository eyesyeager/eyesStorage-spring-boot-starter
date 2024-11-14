package io.github.eyesyeager.eyesStorageStarter.service;

import io.github.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import io.github.eyesyeager.eyesStorageStarter.exception.EyesStorageException;

/**
 * @author artonyu
 * @date 2024-11-08 10:24
 */

public interface OssStorage {
    /**
     * 获取文件简单下载链接
     * @description 无签名，若存储源配置为公开访问，则可用作永久下载链接
     * @param objectName 文件名称
     * @param path 文件保存路径
     * @return URL
     */
    String getSimpleUrl(String objectName, String path);

    /**
     * 获取文件签名下载链接
     * @param objectName 文件名称
     * @param path 文件保存路径
     * @param expire 过期时间（单位s）
     * @return URL
     */
    String getSignedUrl(String objectName, String path, Long expire) throws EyesStorageException;

    /**
     * 获取文件元信息
     * @param objectName 文件名称
     * @param path 文件保存路径
     * @return ObjectMetaData
     */
    ObjectMetaData statObject(String objectName, String path) throws EyesStorageException;

    /**
     * 简单上传文件
     * @param data 文件字节数组
     * @param objectName 文件名称
     * @param path 文件保存路径
     * @return ObjectModel
     */
    ObjectUploadModel putObject(byte[] data, String objectName, String path) throws EyesStorageException;

    /**
     * 简单文件下载
     * @param objectName 文件名称
     * @param path 文件保存路径
     * @return InputStream
     */
    ObjectDownloadModel getObject(String objectName, String path) throws EyesStorageException;

    /**
     * 文件软删除
     * @param objectName 文件名称
     * @param path 文件保存路径
     * @return boolean
     */
    boolean deleteObject(String objectName, String path) throws EyesStorageException;
}
