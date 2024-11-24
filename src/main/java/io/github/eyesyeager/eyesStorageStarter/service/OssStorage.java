package io.github.eyesyeager.eyesStorageStarter.service;

import io.github.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import io.github.eyesyeager.eyesStorageStarter.exception.EyesStorageException;

import java.io.InputStream;
import java.util.Map;

/**
 * @author artonyu
 * date 2024-11-08 10:24
 */

public interface OssStorage {
    /**
     * 获取文件简单下载链接
     * 无签名，若存储源配置为公开访问，则可用作永久下载链接
     *
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @return URL
     */
    String getSimpleUrl(String objectName, String path);

    /**
     * 获取文件签名下载链接
     *
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @param expire     过期时间（单位s）
     * @return URL
     */
    String getSignedUrl(String objectName, String path, Long expire) throws EyesStorageException;

    /**
     * 获取文件元信息
     *
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @return ObjectMetaData
     */
    ObjectMetaData statObject(String objectName, String path) throws EyesStorageException;

    /**
     * 简单上传文件
     *
     * @param data       文件字节数组
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @return ObjectUploadModel
     */
    ObjectUploadModel putObject(byte[] data, String objectName, String path) throws EyesStorageException;

    /**
     * 简单上传文件
     *
     * @param is         文件输入流
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @return ObjectUploadModel
     */
    ObjectUploadModel putObject(InputStream is, String objectName, String path) throws EyesStorageException;

    /**
     * 简单上传文件
     *
     * @param is         文件输入流
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @param length     文件长度
     * @return ObjectUploadModel
     */
    ObjectUploadModel putObject(InputStream is, String objectName, String path, Long length) throws EyesStorageException;

    /**
     * 上传网络文件
     *
     * @since 1.1.0
     * @param netUrl     网络链接
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @return ObjectUploadModel
     */
    ObjectUploadModel putObjectByNetUrl(String netUrl, String objectName, String path) throws EyesStorageException;

    /**
     * 上传网络文件
     *
     * @since 1.1.0
     * @param netUrl     网络链接
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @param headerMap  下载网络文件时的请求头
     * @return ObjectUploadModel
     */
    ObjectUploadModel putObjectByNetUrl(String netUrl, String objectName, String path, Map<String, String> headerMap) throws EyesStorageException;

    /**
     * 上传网络文件
     * @since 1.1.0
     *
     * @param netUrl     网络链接
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @param method     下载网络文件时的方式
     * @param timeout    下载网络文件时的超时时间
     * @param headerMap  下载文件时的请求头
     * @return ObjectUploadModel
     */
    ObjectUploadModel putObjectByNetUrl(String netUrl, String objectName, String path, String method, int timeout, Map<String, String> headerMap) throws EyesStorageException;


    /**
     * 简单文件下载
     *
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @return ObjectDownloadModel
     */
    ObjectDownloadModel getObject(String objectName, String path) throws EyesStorageException;

    /**
     * 下载网络文件
     *
     * @since 1.1.0
     * @param netUrl    网络链接
     * @param headerMap 请求头
     * @return InputStream
     */
    InputStream getObjectByNetUrl(String netUrl, Map<String, String> headerMap) throws EyesStorageException;

    /**
     * 下载网络文件
     *
     * @since 1.1.0
     * @param netUrl    网络链接
     * @param method    请求方式
     * @param timeout   超时时间
     * @param headerMap 请求头
     * @return InputStream
     */
    InputStream getObjectByNetUrl(String netUrl, String method, int timeout, Map<String, String> headerMap) throws EyesStorageException;

    /**
     * 文件删除
     *
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @return boolean
     */
    boolean deleteObject(String objectName, String path) throws EyesStorageException;
}
