package io.github.eyesyeager.eyesStorageStarter.service;

import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import io.github.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import io.github.eyesyeager.eyesStorageStarter.func.RetryFunction;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.OssProperties;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import io.github.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author artonyu
 * @date 2024-11-09 15:11
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
        for(String role : roles) {
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
     * @param path 文件所在目录
     * @param objectName 文件名称
     * @return 文件标识
     */
    protected String buildKey(String path, String objectName) {
        return (StringUtils.isEmpty(path) ? "" : path + "/") + objectName;
    }

    /**
     * 失败重试
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
}
