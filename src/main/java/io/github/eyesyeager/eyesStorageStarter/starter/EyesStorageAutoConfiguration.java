package io.github.eyesyeager.eyesStorageStarter.starter;

import io.github.eyesyeager.eyesStorageStarter.service.EyesOssStorage;
import io.github.eyesyeager.eyesStorageStarter.service.storage.AliyunOssStorage;
import io.github.eyesyeager.eyesStorageStarter.service.storage.MinioOssStorage;
import io.github.eyesyeager.eyesStorageStarter.service.storage.QiniuOssStorage;
import io.github.eyesyeager.eyesStorageStarter.service.storage.TencentOssStorage;
import io.github.eyesyeager.eyesStorageStarter.starter.condition.AliyunStorageCondition;
import io.github.eyesyeager.eyesStorageStarter.starter.condition.EyesStorageCondition;
import io.github.eyesyeager.eyesStorageStarter.starter.condition.MinioStorageCondition;
import io.github.eyesyeager.eyesStorageStarter.starter.condition.QiniuStorageCondition;
import io.github.eyesyeager.eyesStorageStarter.starter.condition.TencentStorageCondition;
import javax.annotation.Resource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author artonyu
 * @date 2024-11-08 10:16
 */

@Configuration
@EnableConfigurationProperties(EyesStorageProperties.class)
@ComponentScan({"com.eyesyeager.eyesStorageStarter.aop"})
public class EyesStorageAutoConfiguration {
    @Resource
    private EyesStorageProperties eyesToolsProperties;

    @Bean
    @Conditional(MinioStorageCondition.class)
    public MinioOssStorage minioOssStorage(){
        return new MinioOssStorage(eyesToolsProperties.getStorageContext(), eyesToolsProperties.getSource().getMinio());
    }

    @Bean
    @Conditional(QiniuStorageCondition.class)
    public QiniuOssStorage qiniuOssStorage(){
        return new QiniuOssStorage(eyesToolsProperties.getStorageContext(), eyesToolsProperties.getSource().getQiniu());
    }

    @Bean
    @Conditional(TencentStorageCondition.class)
    public TencentOssStorage tencentCosOssStorage(){
        return new TencentOssStorage(eyesToolsProperties.getStorageContext(), eyesToolsProperties.getSource().getTencent());
    }

    @Bean
    @Conditional(AliyunStorageCondition.class)
    public AliyunOssStorage aliyunOssStorage() {
        return new AliyunOssStorage(eyesToolsProperties.getStorageContext(), eyesToolsProperties.getSource().getAliyun());
    }

    /**
     * 因为 bean 从上到下加载，而 EyesOssStorage 的加载取决于存储源 bean
     * 因此需要将 EyesOssStorage 的加载放在最下面
     */
    @Bean
    @Conditional(EyesStorageCondition.class)
    public EyesOssStorage eyesOssStorage() {
        return new EyesOssStorage(eyesToolsProperties.getStorageContext());
    }
}
