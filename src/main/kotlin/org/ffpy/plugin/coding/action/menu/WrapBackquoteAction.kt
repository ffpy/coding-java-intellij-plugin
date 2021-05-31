package org.ffpy.plugin.coding.action.menu

import com.intellij.openapi.actionSystem.AnActionEvent
import org.ffpy.plugin.coding.action.BaseReplaceAction
import org.ffpy.plugin.coding.util.CharacterUtils.isDigit
import org.ffpy.plugin.coding.util.CharacterUtils.isLetter
import org.ffpy.plugin.coding.util.CharacterUtils.isLetterOrDigit
import org.ffpy.plugin.coding.util.CharacterUtils.isUpperCase
import org.ffpy.plugin.coding.util.SqlUtils.isMysqlKeyword

/**
 * SQL字段用反引号包裹
 */
class WrapBackquoteAction : BaseReplaceAction() {

    override fun replace(e: AnActionEvent, text: String): String? {
        val newText = StringBuilder(text.length)
        val word = StringBuilder()
        var prevChar = 0.toChar()
        // 是否位于单引号内，用于忽略单引号包含的文本单词
        // 是否位于单引号内，用于忽略单引号包含的文本单词
        var isInSingleQuote = false
        // 是否在注释内，只判断'-- '注释
        // 是否在注释内，只判断'-- '注释
        var isInComment = false

        for (i in text.indices) {
            val ch = text[i]
            if (ch == '\'') {
                if (prevChar != '\\') {
                    isInSingleQuote = !isInSingleQuote
                }
            }
            if (ch == '-') {
                val nextChar: Char = if (i + 1 >= text.length) 0.toChar() else text[i + 1]
                if (prevChar == '-' && nextChar == ' ') {
                    isInComment = true
                }
            }
            if (ch == '\n') {
                isInComment = false
            }
            if (isSepChar(ch)) {
                appendWord(newText, word, !isInSingleQuote && !isInComment)
                newText.append(ch)
                word.delete(0, word.length)
            } else {
                word.append(ch)
            }
            prevChar = ch
        }

        appendWord(newText, word, !isInSingleQuote && !isInComment)
        val newTextStr = newText.toString()

        return if (newTextStr == text) null else newTextStr
    }

    private fun isAllLowerCase(str: StringBuilder): Boolean {
        if (str.isEmpty()) return false
        if (isDigit(str[0])) return false
        for (element in str) {
            if (element == '_') continue
            if (!isLetterOrDigit(element)) return false
            if (isLetter(element) && isUpperCase(element)) return false
        }
        return true
    }

    private fun appendWord(text: StringBuilder, word: StringBuilder, enable: Boolean) {
        if (enable && isAllLowerCase(word) && !isMysqlKeyword(word.toString())) {
            text.append('`').append(word).append('`')
        } else {
            text.append(word)
        }
    }

    private fun isSepChar(ch: Char): Boolean {
        return !isLetterOrDigit(ch) && ch != '\'' && ch != '"' && ch != '`' && ch != '_'
    }
}