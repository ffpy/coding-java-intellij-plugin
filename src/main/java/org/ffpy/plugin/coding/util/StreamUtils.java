package org.ffpy.plugin.coding.util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * {@link Stream}工具类
 */
public class StreamUtils {

    public static <T, R> Predicate<T> distinct(Function<T, R> valueExtract) {
        Set<R> set = new HashSet<>();
        return value -> set.add(valueExtract.apply(value));
    }
}
