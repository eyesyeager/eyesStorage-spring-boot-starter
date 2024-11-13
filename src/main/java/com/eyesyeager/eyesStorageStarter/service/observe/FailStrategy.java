package com.eyesyeager.eyesStorageStarter.service.observe;

import com.eyesyeager.eyesStorageStarter.context.ConfigContext;
import com.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import com.eyesyeager.eyesStorageStarter.func.FailFunction;
import lombok.extern.slf4j.Slf4j;

/**
 * 失败策略
 * @author artonyu
 * @date 2024-11-09 14:16
 */

@Slf4j
public class FailStrategy {
    private final String strategy;

    public FailStrategy(String strategy){
        this.strategy = strategy;
    }

    /**
     * 根据失败策略执行传入方法
     * @param func 具体方法
     */
    public <T> T apply(FailFunction<T> func) throws EyesStorageException {
        if (ConfigContext.FAIL_MODE_FAST.equals(strategy)) {
            // 快速失败
            return func.apply();
        } else if (ConfigContext.FAIL_MODE_SAFE.equals(strategy)) {
            // 安全失败
            try {
                return func.apply();
            } catch (Exception e) {
                log.error("failStrategy apply error", e);
                return null;
            }
        } else {
            throw new RuntimeException("fail-mode " + strategy + " does not exists, please check the configuration.");
        }
    }
}
