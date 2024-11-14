package io.github.eyesyeager.eyesStorageStarter.starter.condition;

import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import io.github.eyesyeager.eyesStorageStarter.starter.AbstractStorageCondition;

/**
 * @author artonyu
 * @date 2024-11-11 11:13
 */

public class AliyunStorageCondition extends AbstractStorageCondition {

    @Override
    public String getSource() {
        return ConfigContext.SOURCE_ALIYUN;
    }
}
