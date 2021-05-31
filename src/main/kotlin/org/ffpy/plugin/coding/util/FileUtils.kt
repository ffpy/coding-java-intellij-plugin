package org.ffpy.plugin.coding.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

object FileUtils {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 查找或者创建目录，支持嵌套创建
     *
     * @param base 基本路径
     * @param path 目录路径
     * @return 目录文件
     */
    fun findOrCreateDirectory(base: VirtualFile, path: String): VirtualFile? {
        if (StringUtils.isEmpty(path)) return base
        val names = StringUtils.split(path.replace('\\', '/'), "/")
        var file: VirtualFile = base
        for (name in names) {
            if (StringUtils.isEmpty(name)) continue
            val f: VirtualFile? = file.findChild(name)
            file = if (f != null) {
                f
            } else {
                val finalFile: VirtualFile = file
                ApplicationManager.getApplication().runWriteAction<VirtualFile>(Computable {
                    try {
                        return@Computable finalFile.createChildDirectory(base, name)
                    } catch (e: IOException) {
                        log.error(e.message, e)
                    }
                    null
                })
            }
        }
        return file
    }

    fun navigateFile(project: Project, file: VirtualFile?) {
        if (file == null) return
        OpenFileDescriptor(project, file).navigate(true)
    }

    fun navigateFileInEditor(project: Project, file: VirtualFile?) {
        if (file == null) return
        OpenFileDescriptor(project, file).navigateInEditor(project, true)
    }

    fun navigateFile(project: Project, directory: VirtualFile, filename: String) {
        directory.findChild(filename)?.let {
            navigateFile(project, it)
        }
    }

    fun navigateFile(project: Project, directory: PsiDirectory, filename: String) {
        directory.findFile(filename)?.virtualFile?.let {
            navigateFile(project, it)
        }
    }

    /**
     * 如果文件存在则忽略，否则把文件添加到文件夹中
     *
     * @param directory 文件夹
     * @param file      文件
     * @return true为不存在，false为已存在
     */
    fun addIfAbsent(directory: PsiDirectory, file: PsiFile): Boolean {
        if (directory.findFile(file.getName()) != null) {
            NotificationHelper.warn("{}已存在", file.getName()).show()
            return false
        }
        directory.add(file)
        return true
    }

    /**
     * 修改文件的内容
     *
     * @param file       文件
     * @param properties 配置文件
     */
    fun setContent(file: VirtualFile, properties: SafeProperties) {
        val bos = ByteArrayOutputStream()
        properties.store(bos, null)
        setContent(file, bos.toByteArray())
    }

    /**
     * 修改文件的内容
     *
     * @param file    文件
     * @param content 内容
     */
    fun setContent(file: VirtualFile, content: ByteArray) {
        file.setBinaryContent(content)
        file.refresh(true, false)
    }
}