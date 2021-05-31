package org.ffpy.plugin.coding.action.menu

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.Node
import org.ffpy.plugin.coding.action.BaseAction
import org.ffpy.plugin.coding.constant.CommentPosition
import org.ffpy.plugin.coding.constant.TemplateName
import org.ffpy.plugin.coding.ui.form.XmlToBeanFormAction
import org.ffpy.plugin.coding.ui.form.XmlToBeanForm
import org.ffpy.plugin.coding.util.CopyPasteUtils
import org.ffpy.plugin.coding.util.FileUtils
import org.ffpy.plugin.coding.util.MyStringUtils
import org.ffpy.plugin.coding.util.StreamUtils
import java.io.IOException
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

/**
 * 根据XML生成对应的Bean
 */
class XmlToBeanAction : BaseAction(), XmlToBeanFormAction {

    override fun action() {
        val form = XmlToBeanForm(env.project, getCopyText())
        form.setAction(this)
        form.pack()
        form.setLocationRelativeTo(null)
        form.isVisible = true
    }

    override fun onOk(doc: Document, packageName: String, position: CommentPosition) {
        parseElement(doc.rootElement, getDirectory(packageName), position)
        env.writeActions.run()
    }

    /**
     * 遍历处理标签，深度优先搜索
     *
     * @param el        当前处理的标签
     * @param directory 生成文件的文件夹
     * @param position  注释位置
     * @return 标签对应的字段信息
     */
    private fun parseElement(el: Element, directory: PsiDirectory, position: CommentPosition): FieldData {
        val comment = if (el.isRootElement) null else getComment(el, position)

        // 叶子标签
        return if (el.isTextOnly) {
            FieldData(normalFieldName(el.name), "String", el.name, comment)
        } else {
            val className = normalClassName(el.name)
            val elements = el.elements()
            val listElements = getListElements(elements)

            // 收集当前标签的子标签
            val fields: List<FieldData> = elements.stream()
                .filter(StreamUtils.distinct { obj: Element -> obj.name })
                .map { e: Element -> parseElement(e, directory, position) }
                .map { field: FieldData -> processList(listElements, field) }
                .collect(Collectors.toList())

            // 模板参数
            val params: MutableMap<String, Any> = HashMap(8)
            params["className"] = className
            params["elementName"] = el.name
            params["isRoot"] = el.isRootElement
            params["fields"] = fields
            params["hasSingle"] = fields.size != listElements.size
            params["hasList"] = !listElements.isEmpty()
            env.writeActions.add {
                val psiFile: PsiFile = env.createJavaFile(TemplateName.XML_TO_BEAN, className, params)
                FileUtils.addIfAbsent(directory, psiFile)
                if (el.isRootElement) {
                    FileUtils.navigateFile(env.project, directory, psiFile.getName())
                }
            }
            FieldData(normalFieldName(el.name), className, el.name, comment)
        }
    }

    /**
     * 处理List字段
     *
     * @param listElements List类型的标签名集合
     * @param field        要处理的字段
     * @return 直接返回field参数
     */
    private fun processList(listElements: Set<String>, field: FieldData): FieldData {
        field.isList = listElements.contains(field.elementName)
        if (field.isList) {
            field.type = "List<${field.type}>"

            // List类型字段的字段名
            if (field.name.endsWith("s")) {
                field.name = field.name + "List"
            } else {
                field.name = field.name + "s"
            }
        }
        return field
    }

    /**
     * 获取是List类型的标签名
     *
     * @param elements 标签列表
     * @return 是List类型的标签名集合
     */
    private fun getListElements(elements: List<Element>): Set<String> {
        val elementCounter: MutableMap<String, Int> = HashMap()
        elements.stream().map { obj: Element -> obj.name }
            .forEach { name: String -> elementCounter[name] = elementCounter.getOrDefault(name, 0) + 1 }
        elementCounter.values.removeIf { count: Int -> count <= 1 }
        return elementCounter.keys
    }

    /**
     * 获取当前标签的注释
     *
     * @param el       标签
     * @param position 注释位置
     * @return 注释，如果没有则返回null
     */
    private fun getComment(el: Element, position: CommentPosition): String? {
        return position.findComment(el)?.text
    }

    /**
     * 获取要生成文件的目录
     *
     * @param packageName 包名
     * @return 目录
     */
    private fun getDirectory(packageName: String): PsiDirectory {
        return try {
            WriteAction.computeAndWait(ThrowableComputable { env.findOrCreateDirectoryByPackageName(packageName) })
                ?: throw Exception("生成包名失败")
        } catch (e: IOException) {
            throw RuntimeException("生成包名失败: " + e.message)
        }
    }

    /**
     * 获取当前剪切板的内容
     *
     * @return 当前剪切板的内容
     */
    private fun getCopyText(): String? {
        return CopyPasteUtils.getString()?.let { if (it.startsWith("<")) it else null }
    }

    /**
     * 标签名转字段名
     *
     * @param name 标签名
     * @return 字段名
     */
    private fun normalFieldName(name: String): String {
        if (name.length < 2) return name.toLowerCase()
        return MyStringUtils.underScoreCase2CamelCase(name).let {
            it.substring(0, 1).toLowerCase() + it.substring(1)
        }
    }

    /**
     * 标签名转类名
     *
     * @param name 标签名
     * @return 类名
     */
    private fun normalClassName(name: String): String {
        if (name.length < 2) return name.toLowerCase()
        return MyStringUtils.underScoreCase2CamelCase(name).let {
            it.substring(0, 1).toUpperCase() + it.substring(1)
        }
    }

    /**
     * 字段信息
     */
    data class FieldData(
        /** 字段名  */
        var name: String,
        /** 字段类型  */
        var type: String,
        /** 标签名  */
        var elementName: String,
        /** 注释  */
        var comment: String? = null,
        /** 是否是List类型  */
        var isList: Boolean = false
    )
}