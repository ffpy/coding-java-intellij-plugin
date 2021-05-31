package org.ffpy.plugin.coding.util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import org.apache.commons.beanutils.ConvertUtils
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

object PsiUtils {
    /**
     * 获取文件的第一个类
     *
     * @param file Java文件
     * @return PsiClass
     */
    fun getClassByFile(file: PsiJavaFile): PsiClass? {
        val classes = file.classes
        return if (classes.isNotEmpty()) classes[0] else null
    }

    /**
     * 获取元素所属的类
     *
     * @param element PsiElement
     * @return PsiClass
     */
    fun getClassByElement(element: PsiElement): PsiClass? {
        return PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
    }

    /**
     * 格式化代码
     */
    fun reformatJavaFile(theElement: PsiElement) {
        WriteCommandAction.runWriteCommandAction(theElement.project) {
            val codeStyleManager = CodeStyleManager.getInstance(theElement.project)
            try {
                codeStyleManager.reformat(theElement)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    /**
     * 创建文档注释
     *
     * @param comment   注释文本
     * @param multiLine 是否为多行注释
     * @return PsiComment
     */
    /**
     * 创建文档注释
     *
     * @param comment 注释文本
     * @return PsiComment
     */
    @JvmOverloads
    fun createPsiDocComment(
        factory: PsiElementFactory, comment: String, multiLine: Boolean = true
    ): PsiComment {
        val commentText = StringBuilder(comment.length)
        if (multiLine) {
            commentText.append("/**\n")
            var fromIndex = 0
            var index: Int
            do {
                index = comment.indexOf('\n', fromIndex)
                if (index == -1) {
                    index = comment.length
                }
                commentText.append("* ").append(comment, fromIndex, index).append("\n")
                fromIndex = index + 1
            } while (index != comment.length)
            commentText.append("*/")
        } else {
            commentText.append("/** ").append(comment).append(" */")
        }
        return factory.createDocCommentFromText(commentText.toString(), null)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getAnnotationValue(annotation: PsiAnnotation?, name: String, type: Class<T>): T? {
        return annotation?.findAttributeValue(name)?.text?.let { text ->
            // 字符串类型的会有双引号包围，要去掉，如value = "xiaoming"，则text="\"xiaoming\""
            val s = if (type == String::class.java && text.length >= 2) text.substring(1, text.length - 1) else text
            ConvertUtils.convert(s, type) as T
        }
    }

    fun getAllSetter(psiClass: PsiClass?): Stream<PsiMethod> {
        return if (psiClass == null) Stream.empty() else Arrays.stream(psiClass.allMethods)
            .filter { method: PsiMethod ->
                val modifierList = method.modifierList
                modifierList.hasModifierProperty(PsiModifier.PUBLIC) &&
                        !modifierList.hasModifierProperty(PsiModifier.STATIC)
            }
            .filter { method: PsiMethod -> method.name.matches(Regex("^set[A-Z].*$")) }
            .filter { method: PsiMethod -> method.parameterList.parametersCount == 1 }
    }

    fun getAllSetterName(psiClass: PsiClass?): Stream<String> {
        return getAllSetter(psiClass)
            .map { method: PsiMethod ->
                val name = method.name
                if (name.startsWith("set")) {
                    return@map StringUtils.uncapitalize(name.substring("set".length))
                } else {
                    return@map name
                }
            }
    }

    fun getAllGetter(psiClass: PsiClass?): Stream<PsiMethod> {
        return if (psiClass == null) Stream.empty() else Arrays.stream(psiClass.allMethods)
            .filter { method: PsiMethod ->
                val modifierList = method.modifierList
                modifierList.hasModifierProperty(PsiModifier.PUBLIC) &&
                        !modifierList.hasModifierProperty(PsiModifier.STATIC)
            }
            .filter { method: PsiMethod ->
                method.name.matches(Regex("^get[A-Z].*$")) ||
                        method.name.matches(Regex("^is[A-Z].*$"))
            }
            .filter { method: PsiMethod -> method.parameterList.parametersCount == 0 }
            .filter { method: PsiMethod ->
                method.returnType != null &&
                        method.returnType!!.presentableText != "void"
            }
    }

    fun getAllGetterName(psiClass: PsiClass?): Stream<String> {
        return getAllGetter(psiClass)
            .map { method: PsiMethod ->
                val name = method.name
                if (name.startsWith("is")) {
                    return@map StringUtils.uncapitalize(name.substring("is".length))
                } else if (name.startsWith("get")) {
                    return@map StringUtils.uncapitalize(name.substring("get".length))
                } else {
                    return@map name
                }
            }
    }

    /**
     * 创建空行
     */
    fun createWhiteSpace(project: Project?): PsiWhiteSpace {
        return PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n\n") as PsiWhiteSpace
    }

    /**
     * 创建换行
     */
    fun createWhiteSpace(project: Project?, n: Int): PsiWhiteSpace {
        require(n >= 1) { "n must be greater than zero." }
        return PsiParserFacade.SERVICE.getInstance(project)
            .createWhiteSpaceFromText(StringUtils.repeat("\n", n)) as PsiWhiteSpace
    }
}