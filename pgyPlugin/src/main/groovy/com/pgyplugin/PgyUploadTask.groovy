package com.pgyplugin


import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class PgyUploadTask extends DefaultTask {
    @Input
    final Property<String> mBuildTypeName = project.objects.property(String)

    @TaskAction
    void upload() {
        def pgyBuild = project.extensions.findByName("uploadBuild") as PgyExtension
        project.android {
            applicationVariants.all { variant ->
                def checkFlavorName
                def productFlavorName = variant.flavorName//获取构建变体名称
                //如果变体名称都为空则默认为1个apk
                if (productFlavorName == "" && pgyBuild.productFlavorNames.isEmpty()) {
                    checkFlavorName = true
                } else {
                    checkFlavorName = pgyBuild.productFlavorNames.contains(productFlavorName)
                }
                //根据mBuildTypeName以及符合条件的productFlavorName上传apk
                if (buildType.name.equalsIgnoreCase(mBuildTypeName.get()) && checkFlavorName) {
                    variant.outputs.all {
                        println "apk路径：" + it.outputFile.getPath()
                        def token = getCOSToken(pgyBuild)//第一步获取token
                        uploadAPK(it.outputFile, token)//第二步上传apk
                        getBuildInfo(token, pgyBuild, productFlavorName + mBuildTypeName.get())//第三步，查询信息
                    }
                }
            }
        }

    }
    /**
     * 获取上传token
     * @param extension 配置参数
     * @return
     */
    Map<String, String> getCOSToken(PgyExtension extension) {
        //非必传参数pgyExtension.params取
        def params = extension.params
        if (params == null) {
            params = new HashMap<String, String>()
        }
        //必传参数 _api_key  ，buildType
        params.put("_api_key", extension.apiKey)
        params.put("buildType", "apk")
        def form = OKHttpUtil.postForm(Constants.COS_TOKEN_URL, params)
//        println "getCOSToken：" + form.toString()
        return form
    }

    /**
     * 上传apk
     * @param apkFile apk路径
     * @param requestParams 请求参数，从上一步getCOSToken中返回
     */
    void uploadAPK(File apkFile, Map requestParams) {
        Map data = (Map) requestParams.get("data")
        def url = data.get("endpoint")
        def params = data.get("params")
        OKHttpUtil.uploadFile(url, params, apkFile)
    }


    /**
     * 查询更新信息，打印基本信息
     * @param requestParams 请求参数
     * @param extension 配置参数
     * @param productFlavorName productFlavorsName + buildTypesName
     * @return
     */
    Map getBuildInfo(Map requestParams, PgyExtension extension, String productFlavorName) {
        Map data = (Map) requestParams.get("data")
        String buildKey = data.get("key")
        //必传参数 _api_key  ，buildKey第一步中返回的key
        Map<String, String> params = new HashMap<>()
        params.put("_api_key", extension.apiKey)
        params.put("buildKey", buildKey)
        def form = OKHttpUtil.postForm(Constants.GET_BUILD_INFO, params)
        def code = form.get("code")
        if (code == 0) {
            def buildInfo = form.get("data")
            if (extension.printAll) {
                println "**************************************"
                println "FlavorNames：" + productFlavorName
                buildInfo.each { key, value ->
                    println key + ": " + value
                }
                println "**************************************"
            } else {
                println "**************************************"
                println "安装包短连接：" + Constants.PGY_BASE_URL + buildInfo.get("buildKey")
                println "FlavorNames：" + productFlavorName
                println "Build：" + buildInfo.get("buildBuildVersion")
                println "更新说明：" + buildInfo.get("buildUpdateDescription")
                println "**************************************"
            }
            return buildInfo
        }
        if (code == 1247) {
            println "getBuildInfo code:1247 Retry"
            sleep(4000)
            return getBuildInfo(requestParams, extension, productFlavorName)
        }
        println "getBuildInfoError：" + form.toString()
        return form
    }

}