package com.eyesyeager.eyesStorageStarter.func;

import com.eyesyeager.eyesStorageStarter.exception.EyesStorageException;

/**
 * @author artonyu
 * @date 2024-11-09 15:24
 */

@FunctionalInterface
public interface RetryFunction<T> {
    T retry() throws Exception;
}
