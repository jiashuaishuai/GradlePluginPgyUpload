package com.pgyplugin


import org.gradle.api.Plugin
import org.gradle.api.Project

class PgyPlugin implements Plugin<Project> {
    PgyExtension pgyBuild

    @Override
    void apply(Project project) {
        pgyBuild = project.extensions.create("uploadBuild", PgyExtension)
        project.afterEvaluate {
            def bts = project.android.buildTypes
            bts.forEach { buildtype ->
                def mBuildTypeName = buildtype.name
                mBuildTypeName = mBuildTypeName.substring(0, 1).toUpperCase() + mBuildTypeName.substring(1)
                project.tasks.register("pgyUpload" + mBuildTypeName, PgyUploadTask) { task ->
                    task.group = "pgyplugin"
                    task.dependsOn("assemble" + mBuildTypeName)
                    task.mBuildTypeName.set(mBuildTypeName)
                }
            }
        }
    }
}

