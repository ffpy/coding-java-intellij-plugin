package org.ffpy.plugin.coding.action

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.file.PsiDirectoryFactory
import org.apache.commons.lang3.StringUtils
import org.ffpy.plugin.coding.constant.TemplateName
import org.ffpy.plugin.coding.service.SettingService
import org.ffpy.plugin.coding.util.*
import org.ffpy.plugin.coding.util.TemplateUtils.fromString

class ActionEnv(val event: AnActionEvent) {

    val curFile: PsiFile? by lazy { event.getData(LangDataKeys.PSI_FILE) }

    val curJavaFile: PsiJavaFile? by lazy { if (curFile is PsiJavaFile) curFile as PsiJavaFile else null }

    val project: Project by lazy { event.project as Project }

    val elementFactory: PsiElementFactory by lazy { JavaPsiFacade.getElementFactory(project) }

    val writeActions: WriteActions by lazy { WriteActions(project) }

    val directoryFactory: PsiDirectoryFactory by lazy { PsiDirectoryFactory.getInstance(project) }

    val fileFactory: PsiFileFactory by lazy { PsiFileFactory.getInstance(project) }

    val projectRootFile: VirtualFile? by lazy { ProjectUtils.getRootFile(project) }

    val editor: Editor? by lazy { event.getData(LangDataKeys.HOST_EDITOR) }

    val curElement: PsiElement? by lazy { event.getData(LangDataKeys.PSI_ELEMENT) }

    val psiManager: PsiManager by lazy { PsiManager.getInstance(project) }

    val curClass: PsiClass? by lazy { curJavaFile?.let { PsiUtils.getClassByFile(it) } }

    val settingService: SettingService by lazy { ServiceManager.getService(project, SettingService::class.java) }

    val selectedClass: PsiClass? by lazy {
        event.getData(LangDataKeys.PSI_ELEMENT)?.let { if (it is PsiClass) it else null }
    }

    val selectedMethod: PsiMethod? by lazy {
        event.getData(LangDataKeys.PSI_ELEMENT)?.let { if (it is PsiMethod) it else null }
    }

    fun getVirtualFilesByName(name: String): VirtualFile? {
        return IndexUtils.getVirtualFilesByName(project, name).firstOrNull()
    }

    fun getFilesByName(name: String): PsiFile? {
        val files = IndexUtils.getFilesByName(project, name)
        return if (files.isNotEmpty()) files[0] else null
    }

    fun createJavaFile(templateName: TemplateName, filename: String, params: Map<String, Any>): PsiFile {
        val content = fromString(settingService.getTemplate(templateName) ?: "", params)
            .replace("\r\n", "\n")
        val file: PsiFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE, content)
        file.name = StringHelper.toString("$filename.java", params)
        return file
    }

    fun findOrCreateDirectoryByPackageName(packageName: String): PsiDirectory? {
        var file: VirtualFile = projectRootFile?.findFileByRelativePath("src/main/java/") ?: return null
        var s = packageName
        while (s.isNotEmpty()) {
            val split = StringUtils.split(s, ".", 2)
            val dirName = split[0]
            s = if (split.size > 1) split[1] else ""
            file = findOrCreateDirectory(file, dirName)
        }
        return directoryFactory.createDirectory(file)
    }

    private fun findOrCreateDirectory(file: VirtualFile, name: String): VirtualFile {
        var child = file.findChild(name)
        if (child == null) {
            child = file.createChildDirectory(this, name)
        }
        return child
    }
}