package org.ffpy.plugin.coding.util

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import java.util.*
import java.util.function.Predicate
import java.util.function.Supplier

class ActionShowHelper private constructor(private val event: AnActionEvent, var isShow: Boolean) {

    fun fileNameMatch(pattern: String): ActionShowHelper {
        if (isShow) {
            isShow = event.getData(LangDataKeys.VIRTUAL_FILE)?.let {
                it.name.matches(Regex(pattern))
            } ?: false
        }
        return this
    }

    fun fileNameEquals(filename: String): ActionShowHelper {
        if (isShow) {
            isShow = Optional.ofNullable(event.getData(LangDataKeys.VIRTUAL_FILE))
                .map { obj: VirtualFile -> obj.name }
                .map { name: String? -> name == filename }
                .orElse(false)
        }
        return this
    }

    fun filePathMatch(pattern: String): ActionShowHelper {
        if (isShow) {
            isShow = event.getData(LangDataKeys.VIRTUAL_FILE)?.let {
                pattern.matches(Regex(it.path))
            } ?: false
        }
        return this
    }

    fun fileMatch(predicate: Predicate<PsiFile>): ActionShowHelper {
        if (isShow) {
            isShow = event.getData(LangDataKeys.PSI_FILE)?.let {
                predicate.test(it)
            } ?: false
        }
        return this
    }

    fun elementType(type: Class<out PsiElement>): ActionShowHelper {
        if (isShow) {
            isShow = Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                .map { element: PsiElement -> type.isAssignableFrom(element.javaClass) }
                .orElse(false)
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : PsiElement?> elementMatch(type: Class<T>, predicate: Predicate<T>): ActionShowHelper {
        if (isShow) {
            isShow = Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                .filter { element: PsiElement -> type.isAssignableFrom(element.javaClass) }
                .map { element: PsiElement -> element as T }
                .map { t: T -> predicate.test(t) }
                .orElse(false)
        }
        return this
    }

    fun classWithAnnotation(qName: String): ActionShowHelper {
        if (isShow) {
            isShow = event.getData(LangDataKeys.PSI_FILE)?.let {
                if (it is PsiJavaFile) it else null
            }?.let {
                PsiUtils.getClassByFile(it)
            }?.hasAnnotation(qName) ?: false
        }
        return this
    }

    fun isJavaFile(): ActionShowHelper {
        if (isShow) {
            isShow = Optional.ofNullable(event.getData(LangDataKeys.PSI_FILE))
                .map { file: PsiFile? -> file is PsiJavaFile }
                .orElse(false)
        }
        return this
    }

    fun isControllerApiMethod(): ActionShowHelper {
        if (isShow) {
            fileNameMatch(".*Controller.java")
                .elementMatch(PsiMethod::class.java) { psiMethod: PsiMethod -> isApiMethod(psiMethod) }
        }
        return this
    }

    fun and(supplier: Supplier<Boolean>): ActionShowHelper {
        if (isShow) {
            isShow = supplier.get()
        }
        return this
    }

    fun update() {
        event.presentation.isEnabled = isShow
    }

    private fun isApiMethod(psiMethod: PsiMethod): Boolean {
        return psiMethod.getAnnotation("org.springframework.web.bind.annotation.GetMapping") != null ||
                psiMethod.getAnnotation("org.springframework.web.bind.annotation.PostMapping") != null ||
                psiMethod.getAnnotation("org.springframework.web.bind.annotation.PutMapping") != null ||
                psiMethod.getAnnotation("org.springframework.web.bind.annotation.DeleteMapping") != null
    }

    companion object {
        fun of(event: AnActionEvent): ActionShowHelper {
            // dumb状态下意味着IDEA正在更新索引，很多功能都不能用
            val enable = event.project?.let { !DumbService.isDumb(it) } ?: false
            return ActionShowHelper(event, enable)
        }

        fun ofDumb(event: AnActionEvent): ActionShowHelper {
            return ActionShowHelper(event, true)
        }
    }
}