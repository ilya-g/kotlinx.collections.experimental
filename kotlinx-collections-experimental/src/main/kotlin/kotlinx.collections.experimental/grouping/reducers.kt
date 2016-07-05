package kotlinx.collections.experimental.grouping

import java.util.*


typealias StepFunction<R, T> = (R, T) -> R

interface RReducer<R, A, in T> : StepFunction<A, T> {
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
        override fun invoke(p1: Acc, p2: B): Acc = r(p1, f(p2))
        override fun final(result: Acc): R = r.final(result)
    }
}*/

//typealias TransducerTA<R, A, B, C> = (RReducer<R, A, B>) -> RReducer<R, A, C>

//public fun <A, B, R, Acc> mapTA(f: (B) -> A): TransducerTA<R, Acc, A, B> = object : TransducerTA<R, Acc, A, B> {
//
//}


object Sum {
    object Ints : Reducer<Int, Int> {
        override fun invoke(p1: Int, p2: Int): Int = p1 + p2
        override fun initial(item: Int): Int = item
    }

    object Longs : Reducer<Long, Long> {
        override fun invoke(p1: Long, p2: Long): Long = p1 + p2
        override fun initial(item: Long): Long = item
    }

    inline fun <T> by(crossinline selector: (T) -> Int) = object : Reducer<Int, T> {
        override fun invoke(p1: Int, p2: T): Int = p1 + selector(p2)
        override fun initial(item: T): Int = selector(item)
    }

    inline fun <T> byLong(crossinline selector: (T) -> Long) = object : Reducer<Long, T> {
        override fun invoke(p1: Long, p2: T): Long = p1 + selector(p2)
        override fun initial(item: T): Long = selector(item)
    }

}

object Count : Reducer<Int, Any?> {
    override fun initial(item: Any?): Int = 1
    override fun invoke(p1: Int, p2: Any?) = p1 + 1
}

object StringJoin {
    fun <T> with(delimiter: String) = object : RReducer<String, StringBuilder, T> {
        override fun initial(item: T): StringBuilder = StringBuilder(item.toString())
        override fun invoke(p1: StringBuilder, p2: T): StringBuilder = p1.append(delimiter).append(p2.toString())
        override fun final(result: StringBuilder): String = result.toString()
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