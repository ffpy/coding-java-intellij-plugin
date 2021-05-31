package org.ffpy.plugin.coding.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object ProjectUtils {
    private const val DEFAULT_PROJECT_NAME = "com.intellij.openapi.project.impl.DefaultProject"

    /**
     * 获取项目的根目录
     *
     * @param project project
     * @return 根目录
     */
    fun getRootFile(project: Project): VirtualFile? {
        return project.projectFile?.parent?.parent
    }

    /**
     * 判断是不是打包插件时的Project
     *
     * @param project project
     * @return true为是，false为否
     */
    fun isDefaultProject(project: Project?): Boolean {
        return project != null && project.javaClass.name.startsWith(DEFAULT_PROJECT_NAME)
    }
}