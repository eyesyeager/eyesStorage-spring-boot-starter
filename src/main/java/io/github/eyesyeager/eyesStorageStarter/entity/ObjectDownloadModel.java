package io.github.eyesyeager.eyesStorageStarter.entity;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author artonyu
 * @date 2024-11-09 16:20
 */

@Data
@AllArgsConstructor
public class ObjectDownloadModel {
    private String key;

    private String objectName;

    private InputStream inputStream;
}
