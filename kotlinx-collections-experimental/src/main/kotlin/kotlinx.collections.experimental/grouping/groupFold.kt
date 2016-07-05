package kotlinx.collections.experimental.grouping



public inline fun <T, K, R> Iterable<T>.groupAggregateBy(keySelector: (T) -> K, operation: (key: K, value: R?, element: T, first: Boolean) -> R): Map<K, R> {
    val result = mutableMapOf<K, R>()
    for (e in this) {
        val key = keySelector(e)
        val value = result[key]
        result[key] = operation(key, value, e, value == null && !result.containsKey(key))
    }
    return result
}

public inline fun <T, K, R> Iterable<T>.groupFoldBy1(keySelector: (T) -> K, initialValueSelector: (K, T) -> R, operation: (K, R, T) -> R): Map<K, R> =
        groupAggregateBy(keySelector) { key, value, e, first -> operation(key, if (first) initialValueSelector(key, e) else value as R, e) }


public inline fun <T, K, R> Iterable<T>.groupFoldBy(crossinline keySelector: (T) -> K): ((key: K, value: R?, element: T, first: Boolean) -> R) -> Map<K, R> = {
    operation -> groupAggregateBy(keySelector, operation)
}


public inline fun <T, K, R> Iterable<T>.groupFoldBy(keySelector: (T) -> K, initialValue: R, operation: (R, T) -> R): Map<K, R> =
        groupAggregateBy<T, K, R>(keySelector, { k, v, e, first -> operation(if (first) initialValue else v as R, e) })

public inline fun <T, K, R> Iterable<T>.groupReduceBy(keySelector: (T) -> K, reducer: Reducer<R, T>): Map<K, R> =
        groupAggregateBy(keySelector, { k, v, e, first -> if (first) reducer.initial(e) else reducer(v as R, e) })

public inline fun <T, K> Iterable<T>.groupCountBy(keySelector: (T) -> K): Map<K, Int> =
        groupFoldBy(keySelector, 0, { acc, e -> acc + 1 })



public inline fun <S, T : S, K> Iterable<T>.groupReduceBy(keySelector: (T) -> K, operation: (K, S, T) -> S): Map<K, S> {
    return groupAggregateBy(keySelector) { key, value, e, first ->
        if (first) e else operation(key, value as S, e)
    }
}

public inline fun <K> Iterable<Int>.groupBySum(keySelector: (Int) -> K): Map<K, Int> =
        groupReduceBy(keySelector, { k, sum, e -> sum + e })

public inline fun <T, K> Iterable<T>.groupBySumBy(keySelector: (T) -> K, valueSelector: (T) -> Int): Map<K, Int> =
        groupFoldBy(keySelector, 0, { acc, e -> acc + valueSelector(e)})


fun <T, K> Iterable<T>.countBy(keySelector: (T) -> K) =
        groupBy(keySelector).mapValues { it.value.fold(0) { acc, e -> acc + 1 } }




fun main(args: Array<String>) {
    val values = listOf("apple", "fooz", "bisquit", "abc", "far", "bar", "foo")
    val keySelector = { s: String -> s[0] }

    val countByChar = values.groupFoldBy(keySelector, 0, { acc, e -> acc + 1 })

    val sumLengthByChar: Map<Char, Int> = values.groupAggregateBy({ it[0] }) { k, v, e, first -> v ?: 0 + e.length }
    val sumLengthByChar2 = values.groupBySumBy( keySelector, { it.length } )
    println(sumLengthByChar2)

    val countByChar2 = values.groupCountBy { it.first() }
    println(countByChar2)


    println(values.groupReduceBy(keySelector, Count))
    println(values.groupReduceBy(keySelector, Sum.by { it.length }))
}

