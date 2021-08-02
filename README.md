# 海康和大华摄像头预览demo

## 环境要求
- 操作系统 Linux amd64 
- Java版本   8
- 海康sdk版本  CH-HCNetSDKV6.1.6.45_build20210302_linux64
- 大华sdk版本  General_NetSDK_ChnEng_JAVA_Linux64_IS_V3.055.0000000.0.R.210602
- 大华播放库   1435334_General_PlaySDK_Eng_Linux64_IS_V3.044.0000000.1.R.210312

## 配置动态链接库

切换到 root 用户，在 /etc/ld.so.conf.d 文件夹下创建一个配置文件 realplay.conf
文件内容为sdk动态链接库的目录
例如程序放在 /home/yuxiang/realplay 目录
那么配置文件内容为下面三行，第一行是大华动态链接库，第二行和第三行是海康的。

> /home/yuxiang/realplay/so/dh
> /home/yuxiang/realplay/so/hk
> /home/yuxiang/realplay/so/hk/HCNetSDKCom

保存配置

以 root 用户执行命令

```java
ldconfig
```

## 运行程序
切换到普通用户，执行测试脚本