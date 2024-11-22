package io.github.eyesyeager.eyesStorageStarter.starter.properties;

import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author artonyu
 * date 2024-11-08 14:05
 */
@Data
public class SourceProperties {

    @NestedConfigurationProperty
    private MinioProperties minio = new MinioProperties();

    @NestedConfigurationProperty
    private QiniuProperties qiniu = new QiniuProperties();

    @NestedConfigurationProperty
    private TencentProperties tencent = new TencentProperties();

    @NestedConfigurationProperty
    private AliyunProperties aliyun = new AliyunProperties();

    /**
     * 根据存储源名获取配置信息
     * @param name 存储源名
     * @return OssProperties
     */
    public OssProperties getPropertiesByName(String name) {
        if (ConfigContext.SOURCE_MINIO.equals(name)) {
            return minio;
        } else if (ConfigContext.SOURCE_QINIU.equals(name)) {
            return qiniu;
        } else if (ConfigContext.SOURCE_TENCENT.equals(name)) {
            return tencent;
        } else if (ConfigContext.SOURCE_ALIYUN.equals(name)) {
            return aliyun;
        } else {
            throw new IllegalArgumentException("there is no storage source named " + name);
        }
    }
}
