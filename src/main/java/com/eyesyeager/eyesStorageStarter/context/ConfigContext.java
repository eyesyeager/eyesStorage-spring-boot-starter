package com.eyesyeager.eyesStorageStarter.context;

/**
 * @author artonyu
 * @date 2024-11-08 10:19
 */

public class ConfigContext {

    /**
     *******************************************************************************
     *                                    配置路径
     *******************************************************************************
     */

    public static final String PACKAGE_ROOT = "eyes-storage";

    /**
     *******************************************************************************
     *                                     存储源
     *******************************************************************************
     */

    public static final String SOURCE_MINIO = "minio";

    public static final String SOURCE_QINIU = "qiniu";

    public static final String SOURCE_TENCENT = "tencent";

    public static final String SOURCE_ALIYUN = "aliyun";

    /**
     *******************************************************************************
     *                                    操作角色
     *******************************************************************************
     */

    public static final String ROLE_READ = "read";

    public static final String ROLE_WRITE = "write";

    public static final String ROLE_DELETE = "delete";

    /**
     *******************************************************************************
     *                                     失败方案
     *******************************************************************************
     */

    public static final String FAIL_MODE_FAST = "fast";

    public static final String FAIL_MODE_SAFE = "safe";

    public static final Integer DEFAULT_FAIL_RETRY_NUM = 0;

    /**
     *******************************************************************************
     *                                     其他
     *******************************************************************************
     */

    // 系统默认时区
    public static final String ZONE_ID = "+08:00";

    // 腾讯云默认传输线程
    public static final Integer TENCENT_DEFAULT_TRANSFER_THREAD_NUM = 1;

    // 腾讯云下载限速
    public static final Integer TENCENT_DEFAULT_DOWNLOAD_TRAFFIC_LIMIT = 100*1024*1024;
}
