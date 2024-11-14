package io.github.eyesyeager.eyesStorageStarter.exception;

/**
 * @author artonyu
 * @date 2024-11-08 14:48
 */

public class EyesStorageException extends Exception {

    public EyesStorageException(String message){
        super(message);
    }

    public EyesStorageException(Exception e){
        super(e);
    }
}
