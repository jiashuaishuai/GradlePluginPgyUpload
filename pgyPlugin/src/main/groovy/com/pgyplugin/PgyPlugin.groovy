package com.pgyplugin

import com.google.gson.Gson
import okhttp3.*
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.concurrent.TimeUnit

class PgyPlugin implements Plugin<Project> {
    PgyExtension pgyBuild

    @Override
    void apply(Project project) {
        pgyBuild = project.extensions.create("uploadBuild", PgyExtension)
        def bts = project.android.buildTypes
        for (int i = 0; i < bts.size(); i++) {
            def mBuildTypeName = bts[i].name
            project.tasks.whenTaskAdded { t ->//循环改project下所有tasks，包括动态生成的
                //根据buildTypeName筛选符合条件的assemble任务
                if (t.name.equalsIgnoreCase("assemble" + mBuildTypeName)) {
                    println "assemble任务" + t.name
                    //根据不同的mBuildTypeName创建蒲公英上传task，
                    project.task("pgyUpload_" + mBuildTypeName, group: "pgyplugin", dependsOn: t) {
                        println "新建任务名：" + "pgyUpload_" + mBuildTypeName
                        doLast {
                            project.android {
                                applicationVariants.all { variant ->
                                    def checkFlavorName = false
                                    def productFlavorName = ""//获取构建变体名称
                                    productFlavors.each { pf ->
                                        productFlavorName = productFlavorName + pf.name
                                    }
                                    //如果变体名称都为空则默认为1个apk
                                    if (productFlavorName == "" && pgyBuild.productFlavorNames.isEmpty()) {
                                        checkFlavorName = true
                                    } else if (!pgyBuild.productFlavorNames.isEmpty() && productFlavorName != "") {
                                        checkFlavorName = pgyBuild.productFlavorNames.contains(productFlavorName)
                                        println "变体名称：" + productFlavorName
                                    }
                                    //根据mBuildTypeName以及符合条件的productFlavorName上传apk
                                    if (buildType.name.equalsIgnoreCase(mBuildTypeName) && checkFlavorName) {
                                        variant.outputs.all {
                                            println "apk路径：" + it.outputFile.getPath()
                                            uploadFile(it.outputFile.getPath(), pgyBuild)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

    }

    private void uploadFile(String apkPath, PgyExtension pgyExtension) {
        if (apkPath == null) {
            throw IllegalStateException("请指定上传的文件")
        }
        def apkFile = new File(apkPath)
        if (!apkFile.exists()) {
            throw IllegalStateException("需要上传的文件不存在:" + apkFile.getAbsolutePath())
        }
        def clientBuilder = new OkHttpClient.Builder()
        clientBuilder.connectTimeout(10, TimeUnit.SECONDS)
        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
        OkHttpClient client = clientBuilder.build()
        def bodyBuilder = new MultipartBody.Builder()
        bodyBuilder.setType(MultipartBody.FORM)
        bodyBuilder.addFormDataPart("_api_key", new String(pgyExtension.apiKey))
        bodyBuilder.addFormDataPart("uKey", new String(pgyExtension.userKey))
        bodyBuilder.addFormDataPart("file",
                apkFile.name,
                RequestBody.create(
                        MediaType.parse("application/vnd.android.package-archive"),
                        apkFile)
        )
        HashMap<String, String> params = pgyExtension.params
        for (String key : params.keySet()) {
            //println("add part key: " + key + " value: " + params.get(key))
            bodyBuilder.addFormDataPart(key, params.get(key))
        }
        def request = new Request.Builder()
                .url(pgyExtension.uploadUrl)
                .post(bodyBuilder.build())
                .build()
        println "上传至蒲公英:" + pgyExtension.pgyer
        def response = client.newCall(request).execute()
        if (response == null || response.body() == null) {
            println "蒲公英上传结果失败"
            return null
        }
        def json = response.body().string()
        response.close()
        def mpJson = new Gson().fromJson(json, Map.class)
        if (mpJson.code.toString() == "0") {
            println "蒲公英安装地址: " + pgyExtension.pgyer + mpJson.data.buildShortcutUrl
        } else {
            println "蒲公英上传失败:" + json
        }

    }
}

