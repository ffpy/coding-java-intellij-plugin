package org.ffpy.plugin.coding.util

import com.sun.istack.Nullable

object SqlUtils {
    /** SQL关键字列表  */
    private var mySqlKeywordSet: Set<String> = setOf(
        // 数据类型
        "tinyint", "smallint", "mediumint", "int", "integer", "bigint", "float",
        "double", "decimal", "date", "time", "year", "datetime", "timestamp",
        "char", "varchar", "tinyblob", "tinytext", "blob", "text", "mediumblob",
        "mediumtext", "longblob", "longtext",
        // 语句
        "select", "update", "insert", "into", "delete", "default", "create", "table",
        "primary", "key", "unsigned", "engine", "charset", "collate", "comment",
        "not", "null", "index", "unique", "alter", "drop", "add", "change", "modify",
        // 函数
        "ascii", "ord", "conv", "bin", "oct", "hex", "concat", "length", "octet_length",
        "char_length", "character_length", "locate", "left", "right", "substring", "mid",
        "ltrim", "rtrim", "trim", "space", "replace", "repeat", "reverse", "abs", "mod",
        "floor", "ceiling", "round", "exp", "log", "log10", "pow", "power", "dayofweek",
        "weekday", "dayofmonth", "dayofyear", "year", "hour", "date_format", "time_format",
        "now", "from_unixtime", "unix_timestamp", "sec_to_time", "time_to_sec",
        // 字符集
        "utf8", "utf8mb4", "utf8mb4_unicode_ci", "utf8mb4_croatian_ci"
    )

    /**
     * 判断字符串是不是MySQL的关键字
     *
     * @param word 字符串
     * @return true为是，false为否
     */
    fun isMysqlKeyword(@Nullable word: String?): Boolean {
        return if (word == null) false else mySqlKeywordSet.contains(word.toLowerCase())
    }
}