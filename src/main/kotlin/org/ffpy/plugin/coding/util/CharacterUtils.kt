package org.ffpy.plugin.coding.util

object CharacterUtils {
    fun isLetter(ch: Char): Boolean {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z'
    }

    fun isDigit(ch: Char): Boolean {
        return ch >= '0' && ch <= '9'
    }

    fun isLetterOrDigit(ch: Char): Boolean {
        return isLetter(ch) || isDigit(ch)
    }

    fun isUpperCase(ch: Char): Boolean {
        return ch >= 'A' && ch <= 'Z'
    }

    fun isLowerCase(ch: Char): Boolean {
        return ch >= 'a' && ch <= 'z'
    }
}