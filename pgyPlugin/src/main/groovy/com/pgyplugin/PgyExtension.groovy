package com.pgyplugin

class PgyExtension {
    String apiKey
    String userKey

    /**
     * 非必须参数
     */
    HashMap<String, String> params = []
    /**
     * 蒲公英上传地址
     */
    String uploadUrl = "https://www.pgyer.com/apiv2/app/upload"
    /**
     * 官网地址
     */
    String pgyer = "https://www.pgyer.com/"
    /**
     * 构建变体全名，如果组合为组合全名，如pubm360，pub和m360组合
     */
    List<String> productFlavorNames = []
}