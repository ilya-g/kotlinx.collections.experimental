package kotlinx.collections.experimental.grouping

import kotlin.jvm.internal.NonVolatileRef.IntRef


public interface Grouping<T, out K> {
    fun iterator(): Iterator<T>
    fun keySelector(element: T): K
}

// Should we provide specialized groupings: IntGrouping, LongGrouping, CharGrouping?

public inline fun <T, K> Iterable<T>.grouping(crossinline keySelector: (T) -> K): Grouping<T, K> = object : Grouping<T, K> {
    override fun iterator(): Iterator<T> = this@grouping.iterator()
    override fun keySelector(element: T): K = keySelector(element)
}

public inline fun <T, K> Sequence<T>.grouping(crossinline keySelector: (T) -> K): Grouping<T, K> = object : Grouping<T, K> {
    override fun iterator(): Iterator<T> = this@grouping.iterator()
    override fun keySelector(element: T): K = keySelector(element)
}

public inline fun <T, K> Array<T>.grouping(crossinline keySelector: (T) -> K): Grouping<T, K> = object : Grouping<T, K> {
    override fun iterator(): Iterator<T> = this@grouping.iterator()
    override fun keySelector(element: T): K = keySelector(element)
}

public inline fun <K> IntArray.grouping(crossinline keySelector: (Int) -> K): Grouping<Int, K> = object : Grouping<Int, K> {
    override fun iterator(): IntIterator = this@grouping.iterator()
    override fun keySelector(element: Int): K = keySelector(element)
}

public inline fun <K> CharSequence.grouping(crossinline keySelector: (Char) -> K): Grouping<Char, K> = object : Grouping<Char, K> {
    override fun iterator(): CharIterator = this@grouping.iterator()
    override fun keySelector(element: Char): K = keySelector(element)
}


public inline fun <T, K, R> Grouping<T, K>.aggregate(operation: (key: K, value: R?, element: T, first: Boolean) -> R): Map<K, R> {
    val result = mutableMapOf<K, R>()
    for (e in this.iterator()) {
        val key = keySelector(e)
        val value = result[key]
        result[key] = operation(key, value, e, value == null && !result.containsKey(key))
    }
    return result
}

public inline fun <T, K, R> Grouping<T, K>.fold(initialValueSelector: (K, T) -> R, operation: (K, R, T) -> R): Map<K, R> =
        aggregate { key, value, e, first -> operation(key, if (first) initialValueSelector(key, e) else value as R, e) }


public inline fun <T, K, R> Grouping<T, K>.fold(initialValue: R, operation: (R, T) -> R): Map<K, R> =
        aggregate { k, v, e, first -> operation(if (first) initialValue else v as R, e) }

public /*inline*/ fun <T, K, R, A> Grouping<T, K>.reduce(reducer: RReducer<R, A, T>): Map<K, R> =
        aggregate<T, K, A> { k, v, e, first -> if (first) reducer.initial(e) else reducer(v as A, e) }
            // finalize values, often a no-op
            .mapValuesInPlace { reducer.final(it.value) }

internal inline fun <K, V, R> Map<K, V>.mapValuesInPlace(f: (Map.Entry<K, V>) -> R): Map<K, R> {
    (this as MutableMap<K, V>).entries.forEach {
        (it as MutableMap.MutableEntry<K, R>).setValue(f(it))
    }
    return (this as MutableMap<K, R>)
}

public inline fun <S, T : S, K> Grouping<T, K>.reduce(operation: (K, S, T) -> S): Map<K, S> =
        aggregate { key, value, e, first ->
            if (first) e else operation(key, value as S, e)
        }

public /*inline*/ fun <T, K> Grouping<T, K>.count(): Map<K, Int> = fold(0) { acc, e -> acc + 1 }

public fun <T, K> Grouping<T,K>.countRef(): Map<K, Int> =
        fold(IntRef()) { acc, e -> acc.apply { this.element += 1 }}
        .mapValues { it.value.element }

public fun <T, K> Grouping<T,K>.countRefInPlace(): Map<K, Int> =
        fold(IntRef()) { acc, e -> acc.apply { this.element += 1 }}
        .mapValuesInPlace { it.value.element }

public /*inline*/ fun <K> Grouping<Int, K>.sum(): Map<K, Int> =
        reduce { k, sum, e -> sum + e }

public inline fun <T, K> Grouping<T, K>.sumBy(valueSelector: (T) -> Int): Map<K, Int> =
        fold(0) { acc, e -> acc + valueSelector(e)}

public inline fun <T, K> Grouping<T, K>.sumByRef(valueSelector: (T) -> Int): Map<K, Int> =
        fold(IntRef()) { acc, e -> acc.apply { element += valueSelector(e)} }
            .mapValues { it.value.element }

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
public inline fun <T, K> Grouping<T, K>.sumByRefInPlace(valueSelector: (T) -> Int): Map<K, Int> =
        fold(IntRef()) { acc, e -> acc.apply { element += valueSelector(e)} }
            .mapValuesInPlace { it.value.element }




fun main(args: Array<String>) {
    val values = listOf("apple", "fooz", "bisquit", "abc", "far", "bar", "foo")
    val grouping = values.grouping { it[0] }

    val countByChar = grouping.fold(0) { acc, e -> acc + 1 }

    val sumLengthByChar: Map<Char, Int> = grouping.aggregate { k, v, e, first -> (v ?: 0) + e.length }
    val sumLengthByChar2 = grouping.sumBy { it.length }
    println(sumLengthByChar2)

    val countByChar2 = values.grouping { it.first() }.count()
    println(countByChar2)

    println(grouping.reduce(Count))
    println(grouping.reduce(Sum.by { it.length }))

    val joined = values.joinToString("")
    val charFrequencies = joined.grouping { it }.count()
    println(charFrequencies.entries.sortedBy { it.key })
}

