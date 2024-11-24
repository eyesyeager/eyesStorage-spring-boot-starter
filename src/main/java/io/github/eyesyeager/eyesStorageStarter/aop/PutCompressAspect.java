package io.github.eyesyeager.eyesStorageStarter.aop;

import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import io.github.eyesyeager.eyesStorageStarter.starter.EyesStorageProperties;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.OssProperties;
import io.github.eyesyeager.eyesStorageStarter.starter.properties.SourceProperties;
import io.github.eyesyeager.eyesStorageStarter.utils.CompressUtils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @author artonyu
 * date 2024-11-13 16:34
 */

@Aspect
@Component
public class PutCompressAspect {

    @Resource
    private EyesStorageProperties properties;

    /**
     * 上传文件压缩
     * @param pjp ProceedingJoinPoint
     */
    @Around("@annotation(io.github.eyesyeager.eyesStorageStarter.aop.PutCompress)")
    public Object doBefore(ProceedingJoinPoint pjp) throws Throwable {
        // 解析注解，获取 source
        MethodSignature signature = (MethodSignature)pjp.getSignature();
        Method method = signature.getMethod();
        PutCompress ann = method.getDeclaredAnnotation(PutCompress.class);
        String source = ann.value();
        if (StringUtils.isEmpty(source)) {
            throw new RuntimeException("the @PutCompress annotation must specify a source when used");
        }
        // 配置参数校验
        SourceProperties sourceProperties = properties.getSource();
        if (Objects.isNull(sourceProperties)) {
            throw new RuntimeException("the project is not configured with source");
        }
        OssProperties property = sourceProperties.getPropertiesByName(source);
        if (Objects.isNull(property)) {
            throw new RuntimeException(source + " storage source is not configured");
        }
        // 若未开启文件压缩上传，则略过
        if (!property.getPutCompress()) {
            return pjp.proceed();
        }
        // 拥有read角色的存储源不可以设置压缩上传
        List<String> roles = property.getRoles();
        if (roles.contains(ConfigContext.ROLE_READ)) {
            throw new RuntimeException("storage sources with the read role cannot be compressed");
        }
        // 执行压缩
        Object[] args = pjp.getArgs();
        if (args[0] instanceof byte[]) {
            args[0] = CompressUtils.compressObject((byte[]) args[0]);
        } else if (args[0] instanceof InputStream) {
            args[0] = CompressUtils.compressObject((InputStream) args[0]);
        } else {
            throw new RuntimeException("only byte[] and InputStream can be compressed");
        }
        args[1] = CompressUtils.getCompressObjectName((String) args[1]);
        return pjp.proceed(args);
    }
}
