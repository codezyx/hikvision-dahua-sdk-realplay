# 海康和大华摄像头预览demo

## 环境要求
- 操作系统 Linux amd64 
- Java版本   8
- 海康sdk版本  CH-HCNetSDKV6.1.6.45_build20210302_linux64
- 大华sdk版本  General_NetSDK_ChnEng_JAVA_Linux64_IS_V3.055.0000000.0.R.210602
- 大华播放库   1435334_General_PlaySDK_Eng_Linux64_IS_V3.044.0000000.1.R.210312

## 配置动态链接库

切换到 root 用户，在 /etc/ld.so.conf.d 文件夹下创建一个配置文件 realplay.conf，
文件内容为sdk动态链接库的绝对路径，
例如程序放在 /home/yuxiang/realplay 目录，
那么配置文件内容为下面三行，第一行是大华动态链接库，第二行和第三行是海康的。

```java
/home/yuxiang/realplay/so/dh
/home/yuxiang/realplay/so/hk
/home/yuxiang/realplay/so/hk/HCNetSDKCom
```

保存配置

以 root 用户执行命令

```java
ldconfig
```

## 运行程序

### 执行脚本 dahua.sh 测试大华摄像头

![dh](http://img.minitool.xyz/github/dh.jpg)

### 执行脚本 haikang.sh 测试海康摄像头

![hk](http://img.minitool.xyz/github/hk.jpg)

## 常见问题说明
### 1.jna版本调整
从大华官网下载的sdk演示项目引用的jna版本是
```xml
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>5.4.0</version>
</dependency>
```
而海康官网sdk演示项目用的jna是

```xml
<dependency>
  <groupId>com.sun.jna</groupId>
  <artifactId>jna</artifactId>
  <version>3.0.9</version>
</dependency>
```

这两个版本api变化很大，新版本的部分api在旧版本里没有。为了兼容最后选择了3.0.9版本的，把大华演示项目中引用的新版api用旧版的api代替实现了。

### 2.海康演示项目引用的examples整合
海康演示项目单独引用了一个maven仓库没有的jar叫做examples.jar，
如果做maven项目还要手动安装依赖到本地仓库，索性就直接复制了几个类到项目中了，引用的类在包
```java
xyz.minitool.sdk.hk.win32
```

### 3.res说明
```java
src/main/resources/res_xx.properties
```
是大华sdk带的，在xyz.minitool.sdk.dh.common里面的Res类引用了

## 部分坑
- 大华播放sdk中，PLAY_GetFreePort接口文档中说的是返回true表示成功，但是实际调用中即使成功也总是返回false，所以这个接口的返回值不能直接用来判断业务逻辑，把返回值忽略吧。
- 大华和海康sdk预览接口在Linux下只能使用callback方式，callback引用必须在界面类成员变量里声明，不然只能预览几秒钟。
- Linux下停止预览后Panel会停留在预览的最后一帧，如果涉及到切换显示不同摄像头预览就会出现，预览第二个摄像头的时候隔几秒显示一次前一个预览的最后一帧，超诡异，只能换种方式实现，每次创建新的Panel。

