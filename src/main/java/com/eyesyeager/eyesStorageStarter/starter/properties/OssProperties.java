package com.eyesyeager.eyesStorageStarter.starter.properties;

import com.eyesyeager.eyesStorageStarter.context.ConfigContext;
import java.util.List;
import lombok.Data;

/**
 * @author artonyu
 * @date 2024-11-08 11:36
 */
@Data
public class OssProperties {

    protected Boolean enable = false;

    /**
     * 操作角色
     * {@link ConfigContext#ROLE_READ} 获取文件及信息时，从该源读取，集群中能且仅能配置一个
     * {@link ConfigContext#ROLE_WRITE} 上传文件时，写入该源，集群中可配置多个
     * {@link ConfigContext#ROLE_DELETE} 通过该源删除文件，集群中可配置多个
     */
    protected List<String> roles;

    protected String endpoint;

    protected String accessKey;

    protected String secretKey;

    protected String bucket;

    /**
     * 文件上传时是否压缩
     */
    protected Boolean putCompress = false;
}
