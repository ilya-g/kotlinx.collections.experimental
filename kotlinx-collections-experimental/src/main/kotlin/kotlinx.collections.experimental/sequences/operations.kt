package kotlinx.collections.experimental.sequences
import kotlinx.collections.experimental.sequences.buildSequence1 as buildSequence

public fun <T, R> Sequence<T>.map_c(transform: (T) -> R): Sequence<R> =
        buildSequence {
            for (element in this@map_c) {
                yield(transform(element))
            }
        }

public inline fun <T, R> Sequence<T>.map_ci(crossinline transform: (T) -> R): Sequence<R> =
        buildSequence {
            for (element in this@map_ci) {
                yield(transform(element))
            }
        }


public fun <T> Sequence<T>.filter_c(predicate: (T) -> Boolean): Sequence<T> =
        buildSequence {
            for (element in this@filter_c) {
//            val iterator = this@filter_c.iterator()
//            while (iterator.hasNext()) {
//                val element = iterator.next()
                if (predicate(element)) yield(element)

            }
        }

public inline fun <T> Sequence<T>.filter_ci(crossinline predicate: (T) -> Boolean): Sequence<T> =
        buildSequence {
            val iterator = this@filter_ci.iterator()
            while (iterator.hasNext()) {
                val element = iterator.next()
                if (predicate(element)) yield(element)

            }
        }

public fun <T> Sequence<T>.filter_std(predicate: (T) -> Boolean): Sequence<T> = FilteringSequence(this, true, predicate)

public fun <T> Sequence<T>.filter_std_ai(predicate: (T) -> Boolean): Sequence<T> = FilteringStdIteratorSequence(this, true, predicate)
public fun <T> Sequence<T>.filter_ai(predicate: (T) -> Boolean): Sequence<T> = FilteringIteratorSequence(this, true, predicate)


public fun <T> Sequence<T>.filter_std_arr(predicate: (T) -> Boolean): Sequence<T> =
        if (this is FilteringSequence)
            MultiFilteringSequence(this.sequence, true, this.predicate, predicate)
        else if (this is MultiFilteringSequence)
            MultiFilteringSequence(this.sequence, true, *this.predicates, predicate)
        else
            FilteringSequence(this, true, predicate)

public fun <T> Sequence<T>.filter_fused(predicate: (T) -> Boolean): Sequence<T> =
        if (this is FilterableSequence)
            this.filter(predicate)
        else
            FilteringSequence(this, true, predicate)


public interface FilterableSequence<T> : Sequence<T> {
    fun filter(predicate: (T) -> Boolean): Sequence<T>
    fun filterNot(predicate: (T) -> Boolean): Sequence<T>
}

public interface MappableSequence<T> : Sequence<T> {
    fun <R> map(transform: (T) -> R): Sequence<R>
}


internal class FilteringSequence<T>(internal val sequence: Sequence<T>,
                                    private val sendWhen: Boolean = true,
                                    val predicate: (T) -> Boolean
) : FilterableSequence<T> {

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
        var nextItem: T? = null

        private fun calcNext() {
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (predicate(item) == sendWhen) {
                    nextItem = item
                    nextState = 1
                    return
                }
            }
            nextState = 0
        }

        override fun next(): T {
            if (nextState == -1)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            val result = nextItem
            nextItem = null
            nextState = -1
            return result as T
        }

        override fun hasNext(): Boolean {
            if (nextState == -1)
                calcNext()
            return nextState == 1
        }
    }

    override fun filter(predicate: (T) -> Boolean): Sequence<T> =
            if (sendWhen == true)
                FilteringSequence(this.sequence, true, { this.predicate(it) && predicate(it) })
            else
                FilteringSequence(this, true, predicate)

    override fun filterNot(predicate: (T) -> Boolean): Sequence<T> =
            if (sendWhen == false)
                FilteringSequence(this.sequence, false, { this.predicate(it) && predicate(it) })
            else
                FilteringSequence(this, false, predicate)
}

internal class MultiFilteringSequence<T>(internal val sequence: Sequence<T>,
                                    private val sendWhen: Boolean = true,
                                    vararg val predicates: (T) -> Boolean
) : Sequence<T> {

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
        var nextItem: T? = null

        private fun calcNext() {
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (predicates.all { it.invoke(item) } == sendWhen) {
                    nextItem = item
                    nextState = 1
                    return
                }
            }
            nextState = 0
        }

        override fun next(): T {
            if (nextState == -1)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            val result = nextItem
            nextItem = null
            nextState = -1
            return result as T
        }

        override fun hasNext(): Boolean {
            if (nextState == -1)
                calcNext()
            return nextState == 1
        }
    }
}


internal class FilteringIteratorSequence<T>(private val sequence: Sequence<T>,
                                    private val sendWhen: Boolean = true,
                                    private val predicate: (T) -> Boolean
) : Sequence<T> {

    override fun iterator(): Iterator<T> = object : AbstractIterator<T>() {

        val iterator = sequence.iterator()
        override fun computeNext() {
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (predicate(item) == sendWhen) {
                    setNext(item)
                    return
                }
            }
            done()
        }
    }
}

internal class FilteringStdIteratorSequence<T>(private val sequence: Sequence<T>,
                                    private val sendWhen: Boolean = true,
                                    private val predicate: (T) -> Boolean
) : Sequence<T> {

    override fun iterator(): Iterator<T> = object : kotlin.collections.AbstractIterator<T>() {

        val iterator = sequence.iterator()
        override fun computeNext() {
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (predicate(item) == sendWhen) {
                    setNext(item)
                    return
                }
            }
            done()
        }
    }
}





// some optimizations for AbstractIterator
public abstract class AbstractIterator<T>: Iterator<T> {
    object States {
        const val Failed = -1
        const val NotReady = 0
        const val Done = 1
        const val Ready = 2
    }
    private var state = States.NotReady
    private var nextValue: T? = null

    override fun hasNext(): Boolean {
        return when (state) {
            States.NotReady -> tryToComputeNext()
            States.Done -> false
            States.Ready -> true
            else -> throw IllegalStateException("Iterator is in invalid state $state")
        }
    }

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        state = States.NotReady
        val result = nextValue as T
        nextValue = null
        return result
    }

    private fun tryToComputeNext(): Boolean {
        state = States.Failed
        computeNext()
        return state == States.Ready
    }

    /**
     * Computes the next item in the iterator.
     *
     * This callback method should call one of these two methods:
     *
     * * [setNext] with the next value of the iteration
     * * [done] to indicate there are no more elements
     *
     * Failure to call either method will result in the iteration terminating with a failed state
     */
    abstract protected fun computeNext(): Unit

    /**
     * Sets the next value in the iteration, called from the [computeNext] function
     */
    protected fun setNext(value: T): Unit {
        nextValue = value
        state = States.Ready
    }

    /**
     * Sets the state to done so that the iteration terminates.
     */
    protected fun done() {
        state = States.Done
    }
}


public fun <T, R> Sequence<T>.map_std(transform: (T) -> R): Sequence<R> {
    return TransformingSequence(this, transform)
}

public fun <T, R> Sequence<T>.map_fused(transform: (T) -> R): Sequence<R> =
        if (this is MappableSequence)
            this.map(transform)
        else
            TransformingSequence(this, transform)




internal class TransformingSequence<T, R>
constructor(private val sequence: Sequence<T>, private val transformer: (T) -> R) : FilterableSequence<R>, MappableSequence<R> {
    override fun iterator(): Iterator<R> = object : Iterator<R> {
        val iterator = sequence.iterator()
        override fun next(): R {
            return transformer(iterator.next())
        }

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }
    }

    override fun filter(predicate: (R) -> Boolean) = TransformingFilteringSequence(sequence, transformer, predicate, true)
    override fun filterNot(predicate: (R) -> Boolean) = TransformingFilteringSequence(sequence, transformer, predicate, false)

    override fun <S> map(transform: (R) -> S): Sequence<S> = TransformingSequence(sequence, { transform(this.transformer(it)) })
}

internal class TransformingFilteringSequence<T, R>
constructor(private val sequence: Sequence<T>, private val transformer: (T) -> R, private val predicate: (R) -> Boolean, private var sendWhen: Boolean = true) : Sequence<R> {

    override fun iterator(): Iterator<R> = object : Iterator<R> {
        val iterator = sequence.iterator()
        var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
        var nextItem: R? = null

        private fun calcNext() {
            while (iterator.hasNext()) {
                val item = transformer(iterator.next())
                if (predicate(item) == sendWhen) {
                    nextItem = item
                    nextState = 1
                    return
                }
            }
            nextState = 0
        }

        override fun next(): R {
            if (nextState == -1)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            val result = nextItem
            nextItem = null
            nextState = -1
            return result as R
        }

        override fun hasNext(): Boolean {
            if (nextState == -1)
                calcNext()
            return nextState == 1
        }
    }
}

