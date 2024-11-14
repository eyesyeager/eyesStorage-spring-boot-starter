package io.github.eyesyeager.eyesStorageStarter.starter.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author artonyu
 * @date 2024-11-11 10:44
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class AliyunProperties extends OssProperties {
    private String region;
}
