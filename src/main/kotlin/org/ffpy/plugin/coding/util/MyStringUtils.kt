package org.ffpy.plugin.coding.util

import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.regex.Pattern

object MyStringUtils {
    /**
     * 驼峰转下划线
     *
     * @param str 字符串
     * @return 转换后的字符串
     */
    fun camelCase2UnderScoreCase(str: String?): String? {
        if (str == null || str.isEmpty()) return str
        val sb = StringBuilder(str.length)
        var prevIsUpperCase = true
        for (i in 0 until str.length) {
            val ch = str[i]
            val isUpperCase = Character.isUpperCase(ch)
            if (!prevIsUpperCase && isUpperCase) {
                sb.append('_')
            }
            sb.append(if (isUpperCase) Character.toLowerCase(ch) else ch)
            prevIsUpperCase = !Character.isLetterOrDigit(ch) || isUpperCase
        }
        return sb.toString()
    }

    /**
     * 下划线转驼峰
     *
     * @param str 字符串
     * @return 转换后的字符串
     */
    fun underScoreCase2CamelCase(str: String): String {
        if (str.isEmpty()) return str
        val sb = StringBuilder(str.length)
        var prevIsUnderline = false
        for (element in str) {
            if (element == '_') {
                prevIsUnderline = true
            } else {
                if (prevIsUnderline) {
                    sb.append(Character.toUpperCase(element))
                    prevIsUnderline = false
                } else {
                    sb.append(element)
                }
            }
        }
        return sb.toString()
    }

    /**
     * 转换为标题样式，如hello_world -> HelloWorld
     *
     * @param str 字符串
     * @return 转换后的字符串
     */
    fun toTitle(str: String): String {
        return StringUtils.capitalize(underScoreCase2CamelCase(str))
    }

    /**
     * 分隔字符串
     *
     * @param str             要分隔的字符串
     * @param separator       分隔符
     * @param excludeFlags    要忽略的包裹字符，如"'，分隔的时候会忽略包裹字符
     * 如split("abd'1,2'cd124,56", ",", "'") => [ "abd'1,2'cd124", "56" ]
     * @param maxBracketDepth 生效的括号深度，-1表示忽略此选项
     * @return 分隔结果
     */
    fun split(str: String, separator: String, excludeFlags: String, maxBracketDepth: Int = -1): Array<String> {
        if (StringUtils.isEmpty(str)) {
            return emptyArray()
        }
        require(!StringUtils.isEmpty(separator)) { "separator cannot be empty" }
        require(!StringUtils.isEmpty(excludeFlags)) { "excludeFlags cannot be empty" }
        val list: MutableList<String> = ArrayList()
        var flagChar = 0.toChar()
        var start = 0
        var bracketDepth = 0
        val charArray = str.toCharArray()
        for (i in charArray.indices) {
            val ch = charArray[i]
            if (excludeFlags.indexOf(ch) != -1) {
                if (flagChar.toInt() == 0) {
                    flagChar = ch
                } else if (ch == flagChar) {
                    flagChar = 0.toChar()
                }
            } else if (flagChar.toInt() == 0) {
                if (ch == '(') {
                    bracketDepth++
                } else if (ch == ')') {
                    bracketDepth--
                }
                if (maxBracketDepth != -1 && maxBracketDepth >= bracketDepth) {
                    val end = i + 1
                    if (end >= separator.length) {
                        if (subEquals(charArray, separator, end - separator.length, end)) {
                            list.add(str.substring(start, end - separator.length))
                            start = end
                        }
                    }
                }
            }
        }
        list.add(str.substring(start))
        return list.toTypedArray()
    }
    /**
     * 查找子字符串的位置
     *
     * @param str             原字符串
     * @param subStr          子字符串
     * @param fromIndex       开始查找的位置
     * @param excludeFlags    忽略的包裹字符
     * @param caseInsensitive 是否忽略大小写
     * @return 子字符串在原字符串的位置，-1代表没有找到
     */
    /**
     * 查找子字符串的位置
     *
     * @param str          原字符串
     * @param subStr       子字符串
     * @param fromIndex    开始查找的位置
     * @param excludeFlags 忽略的包裹字符
     * @return 子字符串在原字符串的位置，-1代表没有找到
     */
    @JvmOverloads
    fun indexOf(
        str: String, subStr: String, fromIndex: Int,
        excludeFlags: String, caseInsensitive: Boolean = false
    ): Int {
        return if (StringUtils.isEmpty(str) || StringUtils.isEmpty(subStr)) {
            -1
        } else indexOf(str.toCharArray(), subStr, fromIndex, excludeFlags, caseInsensitive)
    }
    /**
     * 查找子字符串的位置
     *
     * @param charArray       字符数组
     * @param subStr          子字符串
     * @param fromIndex       开始查找的位置
     * @param excludeFlags    忽略的包裹字符
     * @param caseInsensitive 是否忽略大小写
     * @return 子字符串在原字符串的位置，-1代表没有找到
     */
    /**
     * 查找子字符串的位置
     *
     * @param charArray    字符数组
     * @param subStr       子字符串
     * @param fromIndex    开始查找的位置
     * @param excludeFlags 忽略的包裹字符
     * @return 子字符串在原字符串的位置，-1代表没有找到
     */
    @JvmOverloads
    fun indexOf(
        charArray: CharArray, subStr: String, fromIndex: Int,
        excludeFlags: String, caseInsensitive: Boolean = false
    ): Int {
        if (charArray.isEmpty() || StringUtils.isEmpty(subStr)) {
            return -1
        }
        if (fromIndex < 0 || fromIndex >= charArray.size) {
            throw IndexOutOfBoundsException()
        }
        require(!StringUtils.isEmpty(excludeFlags)) { "excludeFlags cannot be empty" }
        var flagChar = 0.toChar()
        for (i in fromIndex until charArray.size) {
            val ch = charArray[i]
            if (excludeFlags.indexOf(ch) != -1) {
                if (flagChar.toInt() == 0) {
                    flagChar = ch
                } else if (ch == flagChar) {
                    flagChar = 0.toChar()
                }
            } else if (flagChar.toInt() == 0) {
                val end = i + 1
                if (end >= subStr.length) {
                    if (subEquals(charArray, subStr, end - subStr.length, end, caseInsensitive)) {
                        return end - subStr.length
                    }
                }
            }
        }
        return -1
    }
    /**
     * 判断字符数组指定范围的字符串是否与给定字符串相同
     *
     * @param chars           字符数组
     * @param other           要比较的子字符串
     * @param start           开始位置
     * @param end             结束位置，不包括
     * @param caseInsensitive 是否忽略大小写
     * @return true相同，false不同
     */
    /**
     * 判断字符数组指定范围的字符串是否与给定字符串相同
     *
     * @param chars 字符数组
     * @param other 要比较的子字符串
     * @param start 开始位置
     * @param end   结束位置，不包括
     * @return true相同，false不同
     */
    @JvmOverloads
    fun subEquals(chars: CharArray, other: String, start: Int, end: Int, caseInsensitive: Boolean = false): Boolean {
        if (start < 0 || end < 0 || start > end) {
            throw StringIndexOutOfBoundsException()
        }
        if (end - start != other.length) {
            return false
        }
        for (i in start until end) {
            if (caseInsensitive) {
                if (Character.toLowerCase(chars[i]) != Character.toLowerCase(other[i - start])) {
                    return false
                }
            } else {
                if (chars[i] != other[i - start]) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * 包裹字符串，如wrap("ab", "'") => 'ab'
     *
     * @param str     要包裹的字符串
     * @param wrapStr 包裹字符
     * @return 结果字符串
     */
    fun wrap(str: String, wrapStr: String): String {
        return wrapStr + str + wrapStr
    }

    /**
     * 用括号包裹字符串，如wrapWithBrackets("ab") => (ab)
     *
     * @param str 要包裹的字符串
     * @return 结果字符串
     */
    fun wrapWithBrackets(str: String): String {
        return "($str)"
    }

    /**
     * 统计包括中文字符串的显示长度，一个中文字符的显示长度约为1.665个英文字符的显示长度
     *
     * @param str 字符串
     * @return 显示长度
     */
    fun width(str: String): Int {
        val pattern = Pattern.compile("([\\u4E00-\\u9FA5]|[\\uFE30-\\uFFA0])")
        val matcher = pattern.matcher(str)
        var n = 0
        while (matcher.find()) {
            n++
        }
        return str.length + Math.round(n * 0.665).toInt()
    }

    /**
     * 判断字符串中是否有中文（汉字/中文标点符号）
     *
     * @param str 字符串
     * @return true为有，false为没有
     */
    fun hasChinese(str: String?): Boolean {
        val pattern = Pattern.compile("([\\u4E00-\\u9FA5]|[\\uFE30-\\uFFA0])")
        val matcher = pattern.matcher(str)
        return matcher.find()
    }

    /**
     * 判断指定位置所在行的前缀是否为子串
     *
     * @param str    字符串
     * @param prefix 前缀字符串
     * @param index  位置
     * @return true为是，false为否
     */
    fun lineStartsWith(str: String, prefix: String?, index: Int): Boolean {
        var lineStart = 0
        for (i in index downTo 0) {
            if (str[i] == '\n') {
                lineStart = i + 1
                break
            }
        }
        return if (lineStart >= str.length) {
            false
        } else str.startsWith(prefix!!, lineStart)
    }
}