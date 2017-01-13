package kotlinx.collections.experimental.grouping

import java.util.*
import kotlin.jvm.internal.Ref.IntRef


typealias StepFunction<R, T> = (R, T) -> R

interface RReducer<R, A, in T> : StepFunction<A, T> {
    override fun invoke(acc: A, item: T): A
    fun initial(item: T): A
    fun final(result: A): R
}

interface Reducer<R, in T> : RReducer<R, R, T> {
    override fun final(result: R): R = result
}


/*
interface Transducer<B, C> {
    fun <R, A> transform(r: RReducer<R, A, B>): RReducer<R, A, C>
}

public fun <A, B> map(f: (B) -> A): Transducer<A, B> = object : Transducer<A, B> {
    override fun <R, Acc> transform(r: RReducer<R, Acc, A>) = object : RReducer<R, Acc, B> {
        override fun initial(item: B): Acc = r.initial(f(item))
        override fun invoke(acc: Acc, item: B): Acc = r(acc, f(item))
        override fun final(result: Acc): R = r.final(result)
    }
}*/

//typealias TransducerTA<R, A, B, C> = (RReducer<R, A, B>) -> RReducer<R, A, C>

//public fun <A, B, R, Acc> mapTA(f: (B) -> A): TransducerTA<R, Acc, A, B> = object : TransducerTA<R, Acc, A, B> {
//
//}


object Sum {
    object Ints : Reducer<Int, Int> {
        override fun invoke(acc: Int, item: Int): Int = acc + item
        override fun initial(item: Int): Int = item
    }

    object Longs : Reducer<Long, Long> {
        override fun invoke(acc: Long, item: Long): Long = acc + item
        override fun initial(item: Long): Long = item
    }

    inline fun <T> by(crossinline selector: (T) -> Int) = object : Reducer<Int, T> {
        override fun invoke(acc: Int, item: T): Int = acc + selector(item)
        override fun initial(item: T): Int = selector(item)
    }

    inline fun <T> byLong(crossinline selector: (T) -> Long) = object : Reducer<Long, T> {
        override fun invoke(acc: Long, item: T): Long = acc + selector(item)
        override fun initial(item: T): Long = selector(item)
    }
}

object RefSum {
    inline fun <T> by(crossinline selector: (T) -> Int) = object : RReducer<Int, IntRef, T> {
        override fun invoke(acc: IntRef, item: T): IntRef = acc.apply { element += selector(item) }
        override fun initial(item: T): IntRef = IntRef().apply { element = selector(item) }
        override fun final(result: IntRef): Int = result.element
    }
}


object Count : Reducer<Int, Any?> {
    override fun initial(item: Any?): Int = 1
    override fun invoke(acc: Int, item: Any?) = acc + 1
}

object CountWithRef : RReducer<Int, IntRef, Any?> {
    override fun invoke(acc: IntRef, item:  Any?): IntRef = acc.apply { this.element += 1 }
    override fun initial(item:  Any?) = IntRef()
    override fun final(result: IntRef) = result.element
}

object StringJoin {
    fun <T> with(delimiter: String) = object : RReducer<String, StringBuilder, T> {
        override fun initial(item: T): StringBuilder = StringBuilder(item.toString())
        override fun invoke(acc: StringBuilder, item: T): StringBuilder = acc.append(delimiter).append(item.toString())
        override fun final(result: StringBuilder): String = result.toString()
    }
    fun <T> with(delimiter: String, prefix: String, suffix: String) = object : RReducer<String, StringBuilder, T> {
        override fun initial(item: T): StringBuilder = StringBuilder().append(prefix).append(item.toString())
        override fun invoke(acc: StringBuilder, item: T): StringBuilder = acc.append(delimiter).append(item.toString())
        override fun final(result: StringBuilder): String = result.append(suffix).toString()
    }
}



public fun <T, R, A> Iterable<T>.reduce(reducer: RReducer<R, A, T>): R {
    val it = iterator()
    if (!it.hasNext())
        throw NoSuchElementException("Empty collection can't be reduced")

    var accumulator = reducer.initial(it.next())
    for (e in it) {
        accumulator = reducer(accumulator, e)
    }

    return reducer.final(accumulator)
}


fun main(args: Array<String>) {
    val items = listOf(1, 2, 2, 9, 15, 21)
    println(items.reduce(Count))
    println(items.reduce(Sum.Ints))
    println(items.map { it.toLong() }.reduce(Sum.Longs))
    println(items.reduce(StringJoin.with("=")))
}