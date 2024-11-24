package io.github.eyesyeager.eyesStorageStarter.service;

import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import io.github.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import io.github.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import io.github.eyesyeager.eyesStorageStarter.func.RetryFunction;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.OssProperties;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import io.github.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author artonyu
 * date 2024-11-09 15:11
 */
@Slf4j
public abstract class AbstractOssStorage implements OssStorage {

    protected String source;

    protected final EyesStorageProperties.StorageContext context;

    protected final OssProperties properties;

    public AbstractOssStorage(String source, EyesStorageProperties.StorageContext context, OssProperties properties) {
        this.source = source;
        this.properties = properties;
        this.context = context;
    }

    @Resource
    private EyesOssStorage eyesOssStorage;

    /**
     * 观察者注册读写角色
     */
    @PostConstruct
    private void attach() {
        List<String> roles = properties.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return;
        }
        for (String role : roles) {
            if (ConfigContext.ROLE_READ.equals(role)) {
                eyesOssStorage.attachRead(this);
            } else if (ConfigContext.ROLE_WRITE.equals(role)) {
                eyesOssStorage.attachWrite(this);
            } else if (ConfigContext.ROLE_DELETE.equals(role)) {
                eyesOssStorage.attachDelete(this);
            } else {
                throw new RuntimeException("the operation with role " + role + " is not defined");
            }
        }
    }

    /**
     * 构建文件标识
     *
     * @param path       文件所在目录
     * @param objectName 文件名称
     * @return 文件标识
     */
    protected String buildKey(String path, String objectName) {
        return (StringUtils.isEmpty(path) ? "" : path + "/") + objectName;
    }

    /**
     * 失败重试
     *
     * @param func 重试函数
     * @return T
     */
    protected <T> T retry(RetryFunction<T> func) throws EyesStorageException {
        Integer retryNum = context.getFailRetry();
        for (int i = 1; i <= retryNum + 1; i++) {
            try {
                return func.retry();
            } catch (Exception e) {
                log.error("try num " + i + " failed", e);
            }
        }
        throw new EyesStorageException("operation failed, number of attempts: " + retryNum);
    }

    /**
     * 上传网络文件
     * 实现 OssStorage 接口方法
     *
     * @param netUrl     网络链接
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @return ObjectUploadModel
     */
    public ObjectUploadModel putObjectByNetUrl(String netUrl, String objectName, String path) throws EyesStorageException {
        return putObjectByNetUrl(netUrl, objectName, path, new HashMap<>());
    }

    /**
     * 上传网络文件
     * 实现 OssStorage 接口方法
     *
     * @param netUrl     网络链接
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @param headerMap  下载文件时的请求头
     * @return ObjectUploadModel
     */
    public ObjectUploadModel putObjectByNetUrl(String netUrl, String objectName, String path, Map<String, String> headerMap) throws EyesStorageException {
        try {
            InputStream is = getObjectByNetUrl(netUrl, headerMap);
            return putObject(is, objectName, path);
        } catch (Exception e) {
            throw new EyesStorageException(e);
        }
    }

    /**
     * 上传网络文件
     * 实现 OssStorage 接口方法
     *
     * @param netUrl     网络链接
     * @param objectName 文件名称
     * @param path       文件保存路径
     * @param method     下载网络文件时的方式
     * @param timeout    下载网络文件时的超时时间
     * @param headerMap  下载文件时的请求头
     * @return ObjectUploadModel
     */
    public ObjectUploadModel putObjectByNetUrl(String netUrl, String objectName, String path, String method, int timeout, Map<String, String> headerMap) throws EyesStorageException {
        try {
            InputStream is = getObjectByNetUrl(netUrl, path, timeout, headerMap);
            return putObject(is, objectName, path);
        } catch (Exception e) {
            throw new EyesStorageException(e);
        }
    }

    /**
     * 下载网络文件
     * 实现 OssStorage 接口方法
     *
     * @param netUrl    网络链接
     * @param headerMap 请求头
     * @return ObjectDownloadModel
     */
    public InputStream getObjectByNetUrl(String netUrl, Map<String, String> headerMap) throws EyesStorageException {
        return getObjectByNetUrl(
                netUrl,
                ConfigContext.GET_NET_OBJECT_DEFAULT_METHOD,
                ConfigContext.GET_NET_OBJECT_DEFAULT_TIMEOUT,
                headerMap);
    }

    /**
     * 下载网络文件
     * 实现 OssStorage 接口方法
     *
     * @param netUrl    网络链接
     * @param method    请求方式
     * @param timeout   超时时间（ms）
     * @param headerMap 请求头
     * @return InputStream
     */
    public InputStream getObjectByNetUrl(String netUrl, String method, int timeout, Map<String, String> headerMap) throws EyesStorageException {
        return retry(() -> {
            URL url = new URL(netUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(timeout);
            if (Objects.nonNull(headerMap)) {
                for (String key : headerMap.keySet()) {
                    conn.setRequestProperty(key, headerMap.get(key));
                }
            }
            return conn.getInputStream();
        });
    }
}
