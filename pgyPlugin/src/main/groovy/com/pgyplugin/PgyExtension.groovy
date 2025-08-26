package com.pgyplugin

class PgyExtension {
    String apiKey
    /**
     * 非必须参数
     */
    HashMap<String, String> params = [:]

    /**
     * 构建变体全名，如果组合为组合全名，如pubm360，pub和m360组合
     */
    List<String> productFlavorNames = []

    /**
     * 默认打印基本信息，true打印所有信息
     */
    boolean printAll = false

}