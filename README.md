# PgyUpload

### 一键自动打包并上传至蒲公英Gradle插件

## 使用方法

**1. 添加jitpack仓库,添加GradlePlugindPgyUpload插件**
```groovy
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}

dependencies{
       classpath 'com.github.jiashuaishuai:GradlePluginPgyUpload:1.0.3'
}
```

**2. 需要上传的工程目录**

```groovy
apply plugin: "com.jss.pgyupload"
```

**3. 配置相关参数**

```groovy
uploadBuild {
    apiKey = "蒲公英apikey"
    userKey = "蒲公英userKey"
    params = ["key": "value"]//其他非必要参数 如："buildUpdateDescription": "修改bug"
    productFlavorNames = ["pri", "pub"]//想要上传的构建变体全名，如果组合为组合全名，如：pubm360，pub和m360组合，
}
```

如果项目有配置productFlavor务必配置**productFlavorNames** 

**4. Android Studio 右侧打开gradle -> app -> tasks -> pgyplugin 下会生成 pgyUpload_\*\*\*     如：pgyUpload_debug或者pgyUpload_release 等**

**5. 执行要上传的pgyUpload_\*\*\*    task**

等待build控制台输出结果，