# 快速开始

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
  source:
    # 存储源，当前仅支持 minio 和 七牛云
    minio:
      # 是否启用，必须配置
      enable: true
      # 存储源拥有的操作角色
      roles:
          - read      # 读角色：集群中有且仅能有一个，涉及读取文件及元信息操作时，从该源进行
          - write     # 写角色：集群中可以有多个，涉及写入文件及元信息操作时，从该源进行
          - delete    # 删角色：集群中可以有多个，涉及删除文件及元信息操作时，从该源进行
      endpoint: https://xxx.com
      accessKey:
      secretKey:
      bucket:
    qiniu:
      enable: true
      roles:
        - write
      endpoint: https://xxx.com
      accessKey:
      secretKey:
      bucket:
      region:   # 存储区域，必须配置，配置规则见下文
    tencent:
      enable: true
      roles:
        - write
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
# 七牛云特有配置：
#   存储区域
#     - HuaDong 华东
#     - HuaBei 华北
#     - HuaNan 华南
#     - BeiMei 北美
#     - DongNanYa 东南亚
```