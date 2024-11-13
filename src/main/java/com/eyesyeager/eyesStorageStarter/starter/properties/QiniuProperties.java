package com.eyesyeager.eyesStorageStarter.starter.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author artonyu
 * @date 2024-11-08 11:25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QiniuProperties extends OssProperties {
    private String region;
}
