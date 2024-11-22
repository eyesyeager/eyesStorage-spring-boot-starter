package io.github.eyesyeager.eyesStorageStarter.starter.properties;

import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author artonyu
 * date 2024-11-11 10:44
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class TencentProperties extends OssProperties {
    private String region;

    /**
     * 文件传输线程数
     *      自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
     *      对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
     * 默认值：{@link ConfigContext#TENCENT_DEFAULT_TRANSFER_THREAD_NUM}
     */
    private Integer transferThread = ConfigContext.TENCENT_DEFAULT_TRANSFER_THREAD_NUM;

    /**
     * 下载限速，单位：byte/s
     * 默认值：{@link ConfigContext#TENCENT_DEFAULT_DOWNLOAD_TRAFFIC_LIMIT}
     */
    private Integer downloadTrafficLimit = ConfigContext.TENCENT_DEFAULT_DOWNLOAD_TRAFFIC_LIMIT;
}
