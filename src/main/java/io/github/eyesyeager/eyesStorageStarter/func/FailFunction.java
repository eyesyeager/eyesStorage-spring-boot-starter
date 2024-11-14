package io.github.eyesyeager.eyesStorageStarter.func;

import io.github.eyesyeager.eyesStorageStarter.exception.EyesStorageException;

/**
 * @author artonyu
 * @date 2024-11-09 14:25
 */

@FunctionalInterface
public interface FailFunction<T> {
    T apply() throws EyesStorageException;
}
