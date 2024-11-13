package com.eyesyeager.eyesStorageStarter.service;

import com.eyesyeager.eyesStorageStarter.context.ConfigContext;
import com.eyesyeager.eyesStorageStarter.entity.ObjectDownloadModel;
import com.eyesyeager.eyesStorageStarter.entity.ObjectMetaData;
import com.eyesyeager.eyesStorageStarter.entity.ObjectUploadModel;
import com.eyesyeager.eyesStorageStarter.exception.EyesStorageException;
import com.eyesyeager.eyesStorageStarter.service.observe.FailStrategy;
import com.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties.StorageContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author artonyu
 * @date 2024-11-08 15:34
 */

public class EyesOssStorage implements OssStorage {

    private OssStorage readStorage = null;

    private final List<OssStorage> writeStorages = new CopyOnWriteArrayList<>();

    private final List<OssStorage> deleteStorages = new CopyOnWriteArrayList<>();

    private final FailStrategy failStrategy;

    public EyesOssStorage(StorageContext context) {
        failStrategy = new FailStrategy(context.getFailMode());
    }

    /**
     * 注册 read 角色
     * @param storage 存储源
     */
    public synchronized void attachRead(OssStorage storage) {
        if (Objects.nonNull(readStorage)) {
            throw new RuntimeException("source with a role of read can and must be configured with only one.");
        }
        readStorage = storage;
    }

    /**
     * 注册 write 角色
     * @param storage 存储源
     */
    public void attachWrite(OssStorage storage) {
        writeStorages.add(storage);
    }

    /**
     * 注册 delete 角色
     * @param storage 存储源
     */
    public void attachDelete(OssStorage storage) {
        deleteStorages.add(storage);
    }

    @Override
    public String getSimpleUrl(String objectName, String path) {
        return readStorage.getSimpleUrl(objectName, path);
    }

    @Override
    public String getSignedUrl(String objectName, String path, Long expire) throws EyesStorageException {
        return readStorage.getSignedUrl(objectName, path, expire);
    }

    @Override
    public ObjectMetaData statObject(String objectName, String path) throws EyesStorageException {
        return readStorage.statObject(objectName, path);
    }

    @Override
    public ObjectUploadModel putObject(byte[] data, String objectName, String path) throws EyesStorageException {
        ObjectUploadModel result = new ObjectUploadModel("", objectName, (long)data.length, new ArrayList<>());
        for (OssStorage storage : writeStorages) {
            ObjectUploadModel model = failStrategy.apply(() -> storage.putObject(data, objectName, path));
            if (Objects.nonNull(model)) {
                result.getSource().addAll(model.getSource());
                result.setKey(model.getKey());
            }
        }
        return result;
    }

    @Override
    public ObjectDownloadModel getObject(String objectName, String path) throws EyesStorageException {
        return readStorage.getObject(objectName, path);
    }

    @Override
    public boolean deleteObject(String objectName, String path) throws EyesStorageException {
        boolean result = false;
        for (OssStorage storage : deleteStorages) {
            // 只要有一个存储源删除成功就返回 true
            result = result || failStrategy.apply(() -> storage.deleteObject(objectName, path));
        }
        return result;
    }
}
















