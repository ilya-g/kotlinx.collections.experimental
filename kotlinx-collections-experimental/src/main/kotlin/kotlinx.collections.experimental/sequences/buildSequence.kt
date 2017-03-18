@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")
package kotlinx.collections.experimental.sequences

import kotlin.coroutines.experimental.*
import kotlin.coroutines.experimental.intrinsics.*

/**
 *  Builds a [Sequence] lazily yielding values one by one.
 */
public fun <T> buildSequence1(builderAction: suspend SequenceBuilder<T>.() -> Unit): Sequence<T> = Sequence { buildIteratorImpl(builderAction) }


/**
 * Builder for a [Sequence] or an [Iterator], provides [yield] and [yieldAll] suspension functions.
 */
@RestrictsSuspension
public abstract class SequenceBuilder<in T> internal constructor() {
    /**
     * Yields a value to the [Iterator] being built.
     */
    public abstract suspend fun yield(value: T)

    /**
     * Yields all values from the `iterator` to the [Iterator] being built.
     *
     * The sequence of values returned by the given iterator can be potentially infinite.
     */
    public abstract suspend fun yieldAll(iterator: Iterator<T>)

    /**
     * Yields a collections of values to the [Iterator] being built.
     */
    public suspend fun yieldAll(elements: Iterable<T>) {
        if (elements is Collection && elements.isEmpty()) return
        return yieldAll(elements.iterator())
    }

    /**
     * Yields potentially infinite sequence of values  to the [Iterator] being built.
     *
     * The sequence can be potentially infinite.
     */
    public suspend fun yieldAll(sequence: Sequence<T>) = yieldAll(sequence.iterator())
}


internal fun <T> buildIteratorImpl(builderAction: suspend SequenceBuilder<T>.() -> Unit): Iterator<T> {
    val iterator = YieldingIterator<T>()
    iterator.nextStep = builderAction.createCoroutine(receiver = iterator, completion = iterator)
    return iterator
}


private typealias State = Int
private const val State_NotReady: State = 0
private const val State_ManyReady: State = 1
private const val State_Ready: State = 2
private const val State_Done: State = 3
private const val State_Failed: State = 4


private class YieldingIterator<T> : SequenceBuilder<T>(), Iterator<T>, Continuation<Unit> {
    private var state = State_NotReady
    private var nextValue: T? = null
    private var nextIterator: Iterator<T>? = null
    var nextStep: Continuation<Unit>? = null

    override fun hasNext(): Boolean {
        while (true) {
            when (state) {
                State_NotReady -> {}
                State_ManyReady ->
                    if (nextIterator!!.hasNext()) return true else nextIterator = null
                State_Done -> return false
                State_Ready -> return true
                else -> throw exceptionalState()
            }

            state = State_Failed
            val step = nextStep!!
            nextStep = null
            step.resume(Unit)
        }
    }

    override fun next(): T {
        when (state) {
            State_NotReady -> return nextNotReady()
            State_ManyReady -> return nextIterator!!.next()
            State_Ready -> {
                state = State_NotReady
                val result = nextValue as T
                nextValue = null
                return result
            }
            else -> throw exceptionalState()
        }
    }

    private fun nextNotReady(): T {
        if (!hasNext()) throw NoSuchElementException() else return next()
    }

    private fun exceptionalState(): Throwable = when (state) {
        State_Done -> NoSuchElementException()
        State_Failed -> IllegalStateException("Iterator has failed.")
        else -> IllegalStateException("Unexpected state of the iterator: $state")
    }



    suspend override fun yield(value: T) {
        nextValue = value
        state = State_Ready
        return suspendCoroutineOrReturn { c ->
            nextStep = c
            COROUTINE_SUSPENDED
        }
    }

    suspend override fun yieldAll(iterator: Iterator<T>) {
        if (!iterator.hasNext()) return
        nextIterator = iterator
        state = State_ManyReady
        return suspendCoroutineOrReturn { c ->
            nextStep = c
            COROUTINE_SUSPENDED
        }
    }

    // Completion continuation implementation
    override fun resume(value: Unit) {
        state = State_Done
    }

    override fun resumeWithException(exception: Throwable) {
        throw exception // just rethrow
    }

    override val context: CoroutineContext get() = EmptyCoroutineContext
}


public fun <T> buildSequence2(block: suspend SequenceBuilder<T>.() -> Unit): Sequence<T> = object : Sequence<T> {
    override fun iterator(): Iterator<T> {
        val iterator = GeneratorIteratorActual<T>()
        iterator.nextStep = block.createCoroutine(receiver = iterator, completion = iterator)
        return iterator
    }
}

private class GeneratorIteratorActual<T> : SequenceBuilder<T>(), Iterator<T>, Continuation<Unit> {
    var nextStep: Continuation<Unit>? = null
    private var computedNext = false
    private var nextIterator: Iterator<T>? = null
    private var nextValue: T? = null

    override fun hasNext(): Boolean {
        if (!computedNext) {
            nextIterator?.let { nextIt ->
                if (nextIt.hasNext()) {
                    nextValue = nextIt.next()
                    computedNext = true
                    return true
                }
                nextIterator = null
            }
            val step = nextStep!!
            computedNext = true
            nextStep = null
            step.resume(Unit) // leaves it in "done" state if crashes
        }
        return nextStep != null
    }

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        val value = nextValue as T
        computedNext = false
        nextValue = null
        return value
    }

    // Completion continuation implementation
    override fun resume(value: Unit) {
        // nothing to do here -- leave null in nextStep
    }

    override fun resumeWithException(exception: Throwable) {
        throw exception // just rethrow
    }

    override val context: CoroutineContext get() = EmptyCoroutineContext

    // Generator implementation
    override suspend fun yield(value: T) {
        nextValue = value
        return suspendCoroutineOrReturn { c ->
            nextStep = c
            COROUTINE_SUSPENDED
        }
    }

    override suspend fun yieldAll(iterator: Iterator<T>) {
        if (!iterator.hasNext()) return // no values -- don't suspend
        nextValue = iterator.next()
        nextIterator = iterator
        return suspendCoroutineOrReturn { c ->
            nextStep = c
            COROUTINE_SUSPENDED
        }
    }
}

/**
 * Generates lazy sequence.
 */
public fun <T> buildSequence3(block: suspend SequenceBuilder<T>.() -> Unit): Sequence<T> = object : Sequence<T> {
    override fun iterator(): Iterator<T> {
        val iterator = GeneratorIteratorOriginal<T>()
        iterator.nextStep = block.createCoroutine(receiver = iterator, completion = iterator)
        return iterator
    }
}

private class GeneratorIteratorOriginal<T> : SequenceBuilder<T>(), Iterator<T>, Continuation<Unit> {
    var computedNext = false
    var nextStep: Continuation<Unit>? = null
    var nextValue: T? = null

    override fun hasNext(): Boolean {
        if (!computedNext) {
            val step = nextStep!!
            computedNext = true
            nextStep = null
            step.resume(Unit) // leaves it in "done" state if crashes
        }
        return nextStep != null
    }

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        computedNext = false
        return nextValue as T
    }

    // Completion continuation implementation
    override fun resume(value: Unit) {
        // nothing to do here -- leave null in nextStep
    }

    override fun resumeWithException(exception: Throwable) {
        throw exception // just rethrow
    }

    override val context: CoroutineContext get() = EmptyCoroutineContext

    // Generator implementation
    override suspend fun yield(value: T) {
        nextValue = value
        return suspendCoroutineOrReturn { c ->
            nextStep = c
            COROUTINE_SUSPENDED
        }
    }

    override suspend fun yieldAll(iterator: Iterator<T>) {
        if (!iterator.hasNext()) return // no values -- don't suspend
        nextValue = iterator.next()
        return suspendCoroutineOrReturn { c ->
            nextStep = IteratorContinuation(c, iterator)
            COROUTINE_SUSPENDED
        }
    }

    inner class IteratorContinuation(val completion: Continuation<Unit>, val iterator: Iterator<T>) : Continuation<Unit> {
        override fun resume(value: Unit) {
            if (!iterator.hasNext()) {
                completion.resume(Unit)
                return
            }
            nextValue = iterator.next()
            nextStep = this
        }

        override fun resumeWithException(exception: Throwable) {
            throw exception // just rethrow
        }

        override val context: CoroutineContext get() = this@GeneratorIteratorOriginal.context
    }
}