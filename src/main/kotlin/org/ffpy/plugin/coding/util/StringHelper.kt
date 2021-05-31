package org.ffpy.plugin.coding.util

import java.util.*

class StringHelper private constructor(str: String) {
    private var mStr: String
    private val paramMap: MutableMap<String, Any> = HashMap()
    fun param(name: String, value: Any): StringHelper {
        paramMap[name] = value
        return this
    }

    fun params(params: Map<String, Any>?): StringHelper {
        if (params != null && !params.isEmpty()) {
            paramMap.putAll(params)
        }
        return this
    }

    override fun toString(): String {
        if (mStr.isEmpty()) return ""
        mStr = mStr.replace("{{", LEFT)
        mStr = mStr.replace("}}", RIGHT)
        paramMap.forEach { (key: String, value: Any) -> mStr = mStr.replace("{$key}", value.toString()) }
        mStr = mStr.replace(LEFT, "{")
        mStr = mStr.replace(RIGHT, "}")
        return mStr
    }

    companion object {
        private const val LEFT = "\u0001"
        private const val RIGHT = "\u0002"
        fun of(str: String): StringHelper {
            return StringHelper(str)
        }

        fun of(str: String, params: Map<String, Any>?): StringHelper {
            return of(str).params(params)
        }

        fun toString(str: String, params: Map<String, Any>?): String {
            return of(str, params).toString()
        }
    }

    init {
        mStr = Objects.requireNonNull(str)
    }
}