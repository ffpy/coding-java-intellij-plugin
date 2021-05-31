package org.ffpy.plugin.coding.util

import java.util.*
import java.util.function.Function
import java.util.function.Predicate

/**
 * [Stream]工具类
 */
object StreamUtils {
    fun <T, R> distinct(valueExtract: Function<T, R>): Predicate<T> {
        val set: MutableSet<R> = HashSet()
        return Predicate { value: T -> set.add(valueExtract.apply(value)) }
    }
}