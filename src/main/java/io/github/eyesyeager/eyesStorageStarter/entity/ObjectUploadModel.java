package io.github.eyesyeager.eyesStorageStarter.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author artonyu
 * @date 2024-11-08 14:41
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectUploadModel {
    private String key;

    private String objectName;

    private Long size;

    private List<String> source;
}
