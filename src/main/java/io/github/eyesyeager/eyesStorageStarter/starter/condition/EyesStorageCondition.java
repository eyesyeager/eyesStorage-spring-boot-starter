package io.github.eyesyeager.eyesStorageStarter.starter.condition;

import io.github.eyesyeager.eyesStorageStarter.starter.AbstractStorageCondition;

/**
 * @author artonyu
 * date 2024-11-08 15:44
 */

public class EyesStorageCondition extends AbstractStorageCondition {
    @Override
    public String getSource() {
        return String.join(SOURCE_DELIMITER, getAllBuiltInSource());
    }
}
