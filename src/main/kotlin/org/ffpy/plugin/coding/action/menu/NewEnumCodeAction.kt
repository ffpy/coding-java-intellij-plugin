package org.ffpy.plugin.coding.action.menu

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.apache.commons.lang3.StringUtils
import org.ffpy.plugin.coding.action.BaseAction
import org.ffpy.plugin.coding.constant.Constant
import org.ffpy.plugin.coding.constant.TemplateName
import org.ffpy.plugin.coding.ui.dialog.InputDialog
import org.ffpy.plugin.coding.util.*
import org.ffpy.plugin.coding.util.FileUtils.addIfAbsent
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/**
 * 生成EnumCode
 */
class NewEnumCodeAction : BaseAction() {

    companion object {
        private val PREDICATE_UPDATE = Pattern.compile(
            "\\s*`\\w+`\\s+(TINY)?INT\\(\\d*\\)\\s+.*COMMENT\\s+'.+[:：].*'.*",
            Pattern.MULTILINE or Pattern.CASE_INSENSITIVE
        ).asPredicate()

        private val PATTERN_CREATE_TABLE = Pattern.compile(
            "CREATE\\s+TABLE\\s+`(\\w+)`", Pattern.CASE_INSENSITIVE
        )

        private val PREDICATE_CREATE_TABLE = Pattern.compile(
            ".*CREATE\\s+TABLE.*", Pattern.CASE_INSENSITIVE
        ).asPredicate()

        private val PATTERN_COLUMN_NAME = Pattern.compile(
            "^\\s*`(\\w+)`"
        )

        private val PATTERN_COMMENT = Pattern.compile(
            "COMMENT\\s+'.*[:：](.+)'", Pattern.CASE_INSENSITIVE
        )
    }

    override fun action() {
        InputDialog(
            "生成EnumCode",
            "包名",
            null,
            Constant.PATTERN_PACKAGE_NAME,
            InputDialog.Action { packageName: String? ->
                val directory = getDirectory(packageName!!) ?: return@Action false
                val file = createFile(directory) ?: return@Action true
                env.writeActions.add {
                    addIfAbsent(directory, file)
                    FileUtils.navigateFile(env.project, directory, file.name)
                }.run()
                true
            }).show()
    }

    override fun update(e: AnActionEvent) {
        ActionShowHelper.of(e)
            .fileNameMatch(".*\\.sql")
            .and { PREDICATE_UPDATE.test(getLineText(e)) }
            .update()
    }

    /**
     * 获取字段名
     *
     * @param lineText 当前行
     */
    private fun getColumnName(lineText: String): String {
        val matcher = PATTERN_COLUMN_NAME.matcher(lineText)
        return if (matcher.find()) matcher.group(1) else ""
    }

    /**
     * 获取注释
     */
    private fun getComment(lineText: String): String {
        val matcher = PATTERN_COMMENT.matcher(lineText)
        return if (matcher.find()) matcher.group(1) else ""
    }

    /**
     * 获取类名
     *
     * @param lineText 当前行
     */
    private fun getClassName(lineText: String): String {
        return MyStringUtils.toTitle(getTableName().toLowerCase()) +
                MyStringUtils.toTitle(getColumnName(lineText).toLowerCase())
    }

    /**
     * 获取模板参数
     *
     * @param lineText  当前行
     * @param className 类名
     */
    private fun getParams(lineText: String, className: String): Map<String, Any> {
        val params: MutableMap<String, Any> = HashMap()
        params["className"] = className
        params["items"] = getItems(getComment(lineText))
        return params
    }

    /**
     * 获取项目列表
     */
    private fun getItems(comment: String): List<Item> {
        val items: List<Item> = StringUtils.split(comment, ",，").asSequence()
            .mapNotNull {
                val split = StringUtils.split(it, "-")
                if (split.size == 2) Item(split[0], split[1], split[1]) else null
            }.toList()

        // 翻译
        var needTranslate = false
        // 如果没有中文则不需要翻译
        for (item in items) {
            if (MyStringUtils.hasChinese(item.name)) {
                needTranslate = true
                break
            }
        }
        if (needTranslate) {
            try {
                val text = items.asSequence().map { it.name }.reduce { acc, s -> "$acc。$s" } ?: ""
                val result: String = TranslateHelper(env.project).zh2En(text)
                val names = StringUtils.split(result, '.')
                var i = 0
                while (i < names.size && i < items.size) {
                    val name = names[i]
                    if (StringUtils.isNotEmpty(name)) {
                        items[i].name = name.trim { it <= ' ' }.toUpperCase().replace("[^\\w]+".toRegex(), "_")
                    }
                    i++
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        return items
    }

    /**
     * 获取表名
     */
    private fun getTableName(): String {
        val editor: Editor = env.editor ?: return ""
        val caretModel = editor.caretModel
        var line = caretModel.logicalPosition.line
        val document = editor.document
        while (line > 0) {
            val text = EditorUtils.getLineText(document, --line) ?: return ""
            if (PREDICATE_CREATE_TABLE.test(text)) {
                val matcher = PATTERN_CREATE_TABLE.matcher(text)
                return if (matcher.find()) matcher.group(1) else ""
            }
        }
        return ""
    }

    /**
     * 生成Java文件
     *
     * @param directory 文件夹
     */
    private fun createFile(directory: PsiDirectory): PsiFile? {
        val lineText = getLineText(env.event)
        val className = getClassName(lineText)
        val path = "$className.java"
        if (directory.findFile(path) != null) {
            NotificationHelper.info("{}已存在", className).show()
            FileUtils.navigateFile(env.project, directory, path)
            return null
        }
        return env.createJavaFile(
            TemplateName.ENUM_CODE, className,
            getParams(lineText, className)
        )
    }

    /**
     * 获取文件夹
     *
     * @param packageName 包名
     */
    private fun getDirectory(packageName: String): PsiDirectory? {
        return try {
            WriteAction.computeAndWait(ThrowableComputable {
                env.findOrCreateDirectoryByPackageName(
                    packageName
                )
            })
        } catch (e: IOException) {
            throw RuntimeException("创建文件夹失败: " + e.message)
        }
    }

    /**
     * 获取当前行内容
     */
    private fun getLineText(e: AnActionEvent): String {
        return e.getData(LangDataKeys.EDITOR)?.let { EditorUtils.getCurLineText(it) } ?: ""
    }

    data class Item(
        /** 码值  */
        var code: String? = null,

        /** 字段名  */
        var name: String? = null,

        /** 注释  */
        var comment: String? = null
    )
}