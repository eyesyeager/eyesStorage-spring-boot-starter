这是一款专为文件多备份设计的 OSS 工具包，目前对接了阿里云、腾讯云、七牛云和 Minio 的对象存储服务。

# 快速开始

先引入最新版的 maven 包：

```xml
<dependency>
    <groupId>io.github.eyesyeager</groupId>
    <artifactId>eyesStorage-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

然后在配置文件中按如下规则配置：

```yaml
eyes-storage:
  enable: true
  # 失败模式
  #   fast（默认）：快速失败，抛出当前异常，终止运行
  #   safe：安全失败，不抛出当前异常，继续运行
  fail-mode: fast
  # 失败重试次数
  #   凡是涉及调用第三方服务的方法，都有失败重试机制
  #   默认为0，不重试
  fail-retry: 0
  # 存储源，目前支持下列几种存储源
  source:
    tencent:
      enable: true  # 是否启用，必须配置
      # 存储源拥有的操作角色
      roles:
        - read      # 读角色：集群中有且仅能有一个，涉及读取文件及元信息操作时，从该源进行
        - write     # 写角色：集群中可以有多个，涉及写入文件及元信息操作时，从该源进行
        - delete    # 删角色：集群中可以有多个，涉及删除文件及元信息操作时，从该源进行
      endpoint: https://xxx.com
      accessKey:
      secretKey:
      bucket:
      region:
    aliyun:
      enable: true
      roles:
        - write
      endpoint: https://xxx.com
      accessKey:
      secretKey:
      bucket:
      region:
      # 文件上传时是否启用压缩，默认为 false
      # 若存储源拥有 read 角色，则不允许开启
      # 开启压缩后，上传文件时，文件名会自动添加 .zip 后缀
      put-compress: true
    qiniu:
      enable: true
      roles:
        - write
      endpoint: https://xxx.com
      accessKey:
      secretKey:
      bucket:
      region:   # 七牛云的存储区域特殊，配置规则见下文
      put-compress:
    minio:
      enable: true
      roles:
        - write
      endpoint: https://xxx.com
      accessKey:
      secretKey:
      bucket:
      put-compress:

# 七牛云特有配置：
#   存储区域
#     - HuaDong 华东
#     - HuaBei 华北
#     - HuaNan 华南
#     - BeiMei 北美
#     - DongNanYa 东南亚
```

配置完后，即可在项目中使用：

```java
/**
 * 实现了主备存储，可以理解为下面所有具体存储源的集合
 * 例如下面的配置：
 *      source:
 *        tencent:
 *          role:
 *            - read
 *            - write
 *            - delete
 *        aliyun:
 *          role:
 *            - write
 *  此时：
 *    - 读取文件信息、下载文件：从 tencent 源进行
 *    - 上传文件：同时上传到 tencent 与 aliyun
 *    - 删除文件：仅删除 tencent
 */
@Resource
private EyesOssStorage storage;

/**
 * 仅操作 tencent 源，其操作不受 role 配置限制
 */
@Resource
private TencentOssStorage storage;

/**
 * 仅操作 aliyun 源，其操作不受 role 配置限制
 */
@Resource
private AliyunOssStorage storage;

/**
 * 仅操作 qiniu 源，其操作不受 role 配置限制
 */
@Resource
private QiniuOssStorage storage;

/**
 * 仅操作 minio 源，其操作不受 role 配置限制
 */
@Resource
private MinioOssStorage storage;

/**
 * EyesStorageProperties 存储 eyes-storage 下的所有配置信息
 * 若内置的 bean 无法满足业务需求，可使用配置信息自行封装工具类
 */
@Resource
private EyesStorageProperties properties;

@Test
public void putObject() {
    try {
        byte[] data = "eyesYeager".getBytes(StandardCharsets.UTF_8);
        ObjectUploadModel model = storage.putObject(data, "world.txt", "hello");
        System.out.println(model);
    } catch (EyesStorageException e) {  
        e.printStackTrace();
    }
}
```
