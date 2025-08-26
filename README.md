# PgyUpload

### 一键自动打包并上传至蒲公英Gradle插件

## 使用方法
**支持配置FlavorNames ，支持flavors组合模式**

## V2.0.1
**更新内容：**
更新V2.0快速上传api
uploadBuild **删除userKey** 新增  **printAll = false**//默认false 只打印基本信息；等于true 打印所有的返回信息
gradle 8.0更新

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
       classpath 'com.github.jiashuaishuai:GradlePluginPgyUpload:2.0.1'
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
    userKey = "蒲公英userKey"//v2.0.1删除此配置项
    params = ["key": "value"]//其他非必要参数 如："buildUpdateDescription": "修改bug"
    productFlavorNames = ["pri", "pub"]//想要上传的构建变体全名，如果组合为组合全名，如：pubm360，pub和m360组合，
    printAll = false//默认false 只打印基本信息；等于true 打印所有的返回信息   v2.0.1新增
}
```

如果项目有配置productFlavor务必配置**productFlavorNames** 

**4. Android Studio 右侧打开gradle -> app -> tasks -> pgyplugin 下会生成 pgyUpload_\*\*\*     如：pgyUpload_debug或者pgyUpload_release 等**

**5. 执行要上传的pgyUpload_\*\*\*    task**

等待build控制台输出结果，
