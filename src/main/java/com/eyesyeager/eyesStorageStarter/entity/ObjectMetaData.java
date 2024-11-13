package com.eyesyeager.eyesStorageStarter.entity;

import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Data;

/**
 * @author artonyu
 * @date 2024-11-08 10:27
 */

@Data
public class ObjectMetaData {

    /**
     * 对象唯一标识
     */
    private String key;

    /**
     * 文件大小（单位：b）
     */
    private Long size;

    /**
     * 对象内容类型
     */
    private String contentType;

    /**
     * 最后修改时间
     */
    private ZonedDateTime lastModified;

    /**
     * 版本号
     */
    private String versionId;

    /**
     * 校验码
     */
    private String etag;

    /**
     * 是否软删除
     */
    private Boolean isDelete;

    /**
     * 自定义的元数据
     */
    private Map userMetadata;
}