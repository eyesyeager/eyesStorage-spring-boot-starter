package com.eyesyeager.eyesStorageStarter.starter;

import com.eyesyeager.eyesStorageStarter.context.ConfigContext;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author artonyu
 * @date 2024-11-11 10:49
 */

public abstract class AbstractStorageCondition implements Condition {
    protected static final String SOURCE_DELIMITER = ",";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 未配置存储 或者 未开启存储，则不加载 bean
        if(!isStorageEnabled(context)) {
            return false;
        }
        String source = getSource();
        if (StringUtils.isEmpty(source)) {
            throw new RuntimeException("storage source is empty.");
        }
        String[] sourceArr = source.split(SOURCE_DELIMITER);
        // 若只有一个存储源，则验证该存储源是否开启
        if (sourceArr.length == 1) {
            return isSourceEnabled(context, source);
        }
        // 当配置多个存储源时，满足一个即可
        for (String item : sourceArr) {
            if (isSourceEnabled(context, item)) {
                return true;
            }
        }
        throw new RuntimeException("no valid storage configuration is configured");
    }

    /**
     * 获取存储源
     * @return 存储源
     */
    public abstract String getSource();

    /**
     * 获取所有内建存储源
     * @return 内建存储源列表
     */
    protected List<String> getAllBuiltInSource() {
        return Arrays.asList(
                ConfigContext.SOURCE_MINIO,
                ConfigContext.SOURCE_QINIU,
                ConfigContext.SOURCE_TENCENT,
                ConfigContext.SOURCE_ALIYUN);
    }

    /**
     * 判断是否开启存储服务
     * @param context ConditionContext
     * @return boolean
     */
    private boolean isStorageEnabled(ConditionContext context) {
        Environment environment = context.getEnvironment();
        String property = environment.getProperty(ConfigContext.PACKAGE_ROOT + ".enable");
        return Objects.equals(property, "true");
    }

    /**
     * 判断制定存储源是否配置
     * @param context ConditionContext
     * @param source 存储源
     * @return boolean
     */
    private boolean isSourceEnabled(ConditionContext context, String source) {
        Environment environment = context.getEnvironment();
        String property = environment.getProperty(ConfigContext.PACKAGE_ROOT + ".source." + source + ".enable");
        return Objects.equals(property, "true");
    }
}
