package com.eyesyeager.eyesStorageStarter.starter;

import com.eyesyeager.eyesStorageStarter.context.ConfigContext;
import com.eyesyeager.eyesStorageStarter.starter.properties.SourceProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author artonyu
 * @date 2024-11-08 10:17
 */

@Data
@Component
@ConfigurationProperties(ConfigContext.PACKAGE_ROOT)
public class EyesStorageProperties {
    /**
     * 是否启用
     */
    private Boolean enable = false;

    /**
     * 失败模式
     * {@link ConfigContext#FAIL_MODE_FAST} 快速失败，抛出当前异常，终止运行
     * {@link ConfigContext#FAIL_MODE_SAFE} 安全失败，不抛出当前异常，继续运行
     */
    private String failMode = ConfigContext.FAIL_MODE_FAST;

    /**
     * 失败重试次数
     */
    private Integer failRetry = ConfigContext.DEFAULT_FAIL_RETRY_NUM;

    @NestedConfigurationProperty
    private SourceProperties source = new SourceProperties();

    /**
     * 获取存储配置上下文
     * @return StorageContext
     */
    public StorageContext getStorageContext() {
        return new StorageContext(this.enable, this.failMode, this.failRetry);
    }

    @Data
    @AllArgsConstructor
    public static class StorageContext {
        private Boolean enable;
        private String failMode;
        private Integer failRetry;
    }
}
