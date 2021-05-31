package org.ffpy.plugin.coding.action.menu

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.editor.Editor
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.ffpy.plugin.coding.action.BaseAction
import org.ffpy.plugin.coding.util.ActionShowHelper
import org.ffpy.plugin.coding.util.MyStringUtils
import java.util.*
import java.util.function.Consumer
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 格式化SQL脚本
 */
class FormatSqlAction : BaseAction() {

    companion object {
        private val PATTERN_FORMAT_INSERT = Pattern.compile(
            "^([^(]*?)\\(([\\s\\S]*)\\)\\s*(VALUES)\\s*([\\s\\S]*)$",
            Pattern.MULTILINE or Pattern.CASE_INSENSITIVE
        )

        private val EMPTY_BOOLEAN_ARRAY = BooleanArray(0)
    }

    override fun action() {
        val editor: Editor = env.editor ?: return

        val doc = editor.document
        val selectionModel = editor.selectionModel
        val hasSelection = selectionModel.hasSelection()
        val selectionStart: Int
        val selectionEnd: Int
        val text: String
        if (hasSelection) {
            // 选择模式
            selectionStart = selectionModel.selectionStart
            selectionEnd = selectionModel.selectionEnd
            text = selectionModel.selectedText ?: return
        } else {
            // 全文模式
            selectionStart = 0
            selectionEnd = 0
            text = doc.text
        }

        val resultText = applyInsert(text)

        // 写入文件
        env.writeActions.add {
            if (hasSelection) {
                doc.replaceString(selectionStart, selectionEnd, resultText)
            } else if (text != resultText) {
                doc.setText(resultText)
            }
        }.run()
    }

    override fun update(e: AnActionEvent) {
        ActionShowHelper.of(e)
            .fileNameMatch(".*\\.sql")
            .and { e.getData(LangDataKeys.EDITOR) != null }
            .update()
    }

    /**
     * 处理插入语句
     *
     * @param text 源文本
     * @return 处理结果文本
     */
    private fun applyInsert(text: String): String {
        val endStr = ");"
        val insertStatement = "INSERT"
        val resultText = StringBuilder()
        var index = 0
        var from = 0
        do {
            index = MyStringUtils.indexOf(text, insertStatement, index, "\"'", true)
            if (index == -1) break

            // 忽略注释
            if (MyStringUtils.lineStartsWith(text, "--", index) ||
                MyStringUtils.lineStartsWith(text, "#", index)
            ) {
                index += insertStatement.length
                continue
            }
            val start = index
            var end = index
            do {
                end = MyStringUtils.indexOf(text, endStr, end, "\"'", false)
                if (!MyStringUtils.lineStartsWith(text, "--", end) &&
                    !MyStringUtils.lineStartsWith(text, "#", end)
                ) {
                    break
                }
            } while (end != -1)
            if (end != -1) {
                resultText.append(text, from, index)
                end += endStr.length
                index = end
                from = end
                val sql = text.substring(start, end)
                resultText.append(formatInsert(sql))
            } else {
                break
            }
        } while (index >= 0 && index < text.length)
        if (from < text.length) {
            resultText.append(text, from, text.length)
        }
        return resultText.toString()
    }

    /**
     * 格式化INSERT语句
     */
    private fun formatInsert(sql: String): String {
        val matcher: Matcher = PATTERN_FORMAT_INSERT.matcher(sql)
        if (!matcher.find()) {
            return sql
        }
        val insertStatement = matcher.group(1).trim()
        var columnsStr = matcher.group(2).trim()
        val valueKeyword = matcher.group(3).trim()
        var valuesStr = matcher.group(4).trim()
        val columns = getColumns(columnsStr)
        val values = getValues(valuesStr)
        val widths = getWidths(columns, values)
        val isLeftAlign = getIsLeftAlign(values)

        fillColumnsBlank(columns, widths)
        fillValuesBlank(values, widths, isLeftAlign)

        columnsStr = MyStringUtils.wrapWithBrackets(StringUtils.join(columns, ", "))

        valuesStr = StringUtils.join(
            values.stream()
                .map { item: Array<String>? ->
                    MyStringUtils.wrapWithBrackets(
                        StringUtils.join(item, ", ")
                    )
                }.toArray(), ",\n"
        ).trim()
        if (StringUtils.isNotEmpty(valuesStr)) {
            valuesStr += ";"
        }
        return "$insertStatement\n$columnsStr\n$valueKeyword\n$valuesStr"
    }

    /**
     * 获取字段
     */
    private fun getColumns(columnsStr: String): Array<String> {
        return Arrays.stream(StringUtils.split(columnsStr, ","))
            .map { column: String ->
                column.trim().replace("`", "")
            }
            .map { column: String? ->
                MyStringUtils.wrap(
                    column!!, "`"
                )
            }
            .toArray { arrayOfNulls<String>(it) }
    }

    /**
     * 获取值列表
     */
    private fun getValues(valuesStr: String): List<Array<String>?> {
        return MyStringUtils.split(valuesStr, ",", "'\"", 0).asSequence()
            .map { it.trim() }
            .map {
                val line = if (it.startsWith("(")) it.substring(1) else it
                return@map when {
                    line.endsWith(");") -> line.substring(0, line.length - 2)
                    line.endsWith(")") -> line.substring(0, line.length - 1)
                    else -> line
                }
            }
            .map { it.trim() }
            .map { column ->
                MyStringUtils.split(column, ",", "'\"", 0).asSequence()
                    .map { it.trim() }
                    .toList()
                    .toTypedArray()
            }.toList()
    }

    /**
     * 判断列是否为字符串类型
     */
    private fun getIsLeftAlign(values: List<Array<String>?>): BooleanArray {
        if (CollectionUtils.isEmpty(values)) {
            return EMPTY_BOOLEAN_ARRAY
        }
        val arrays = BooleanArray(values[0]?.size ?: 0)
        for (value in values) {
            var i = 0
            while (i < value!!.size && i < arrays.size) {
                val item = value[i]
                if (!"NULL".equals(item, ignoreCase = true) && !item.matches(Regex("[\\d.]+"))) {
                    arrays[i] = true
                }
                i++
            }
        }
        return arrays
    }

    /**
     * 计算各列的最大宽度
     */
    private fun getWidths(columns: Array<String>, values: List<Array<String>?>): IntArray {
        val lengths = IntArray(columns.size)
        for (i in lengths.indices) {
            lengths[i] = Math.max(MyStringUtils.width(columns[i]), values.stream()
                .map { it: Array<String>? ->
                    if (i < it!!.size) MyStringUtils.width(
                        it[i]
                    ) else 0
                }
                .max(Comparator.comparingInt { obj: Int -> obj })
                .orElse(0)
            )
        }
        return lengths
    }

    /**
     * 填充字段空白
     */
    private fun fillColumnsBlank(columns: Array<String>, lengths: IntArray) {
        for (i in columns.indices) {
            val column = columns[i]
            val len = lengths[i]
            val n = len - MyStringUtils.width(column)
            if (n > 0) {
                columns[i] = column + StringUtils.repeat(' ', n)
            }
        }
    }

    /**
     * 填充值空白
     */
    private fun fillValuesBlank(values: List<Array<String>?>, lengths: IntArray, isLeftAlign: BooleanArray) {
        values.forEach(Consumer { value: Array<String>? ->
            for (i in value!!.indices) {
                val item = value[i]
                val len = lengths[i]
                val n = len - MyStringUtils.width(item)
                if (n > 0) {
                    if (i < isLeftAlign.size && isLeftAlign[i]) {
                        value[i] = item + StringUtils.repeat(' ', n)
                    } else {
                        value[i] = StringUtils.repeat(' ', n) + item
                    }
                }
            }
        })
    }
}