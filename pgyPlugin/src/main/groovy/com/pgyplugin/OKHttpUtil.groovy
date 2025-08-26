package com.pgyplugin

import groovy.json.JsonSlurper
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

import java.util.concurrent.TimeUnit

class OKHttpUtil {

    private static OkHttpClient okHttpClient
    static {
        //创建okhttp client
        def clientBuilder = new OkHttpClient.Builder()
        clientBuilder.connectTimeout(10, TimeUnit.SECONDS)
        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
        okHttpClient = clientBuilder.build()
    }

    //创建请求参数；蒲公英v2.0上传接口要求：Content-Type: application/x-www-form-urlencoded
    static Map postForm(String url,HashMap<String, String> params = [:]) {
        def formBody = new FormBody.Builder()
        params.forEach { key, value ->
//            println("add part key: " + key + " value: " + value)
            formBody.add(key, value)
        }
        def request = new Request.Builder()
                .url(url)
                .post(formBody.build())
                .build()
        //发送请求
        try (def execute = okHttpClient.newCall(request).execute()) {
            if (execute == null || execute.body() == null) {
                println "蒲公英上传结果失败"
                return [:]
            }
            def json = execute.body().string()
            execute.close()
            return new JsonSlurper().parseText(json)
        } catch (Exception e) {
            println("error: " + e.message)
            return [:]
        }

    }

    static uploadFile(String url,Map<String, String> formData = [:],File file) {
        def bodyBuilder = new MultipartBody.Builder()
        bodyBuilder.setType(MultipartBody.FORM)
        formData.forEach { key, value ->
            bodyBuilder.addFormDataPart(key, value)
        }
        bodyBuilder.addFormDataPart(
                "file",
                file.name,
                RequestBody.create(file, MediaType.parse("application/octet-stream"))
        )
        def request = new Request.Builder()
                .url(url)
                .post(bodyBuilder.build())
                .build()
        try (def execute = okHttpClient.newCall(request).execute()) {
            if (execute == null) {
                println "蒲公英上传结果失败"
                return
            }
            def code = execute.code()
            def msg = execute.message()
            if (204 == code){
                println "上传成功" + msg
            }else {
                println "上传失败："+ msg
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

}

