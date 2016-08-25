package kotlinx.collections.experimental.sequences

fun <T> generate(coroutine c: GeneratorController<T>.() -> Continuation<Unit>): Sequence<T> =
        Sequence { GeneratorController<T>(c) }

class GeneratorController<T> internal constructor(coroutine: GeneratorController<T>.() -> Continuation<Unit>) : Iterator<T> {
    private var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
    private var nextItem: T? = null
    private var nextStep: Continuation<Unit> = this.coroutine()

    private fun calcNext() {
        nextStep.resume(Unit)
    }

    suspend fun yield(value: T, c: Continuation<Unit>) {
        nextItem = value
        nextState = 1
        nextStep = c
    }

    operator fun handleResult(result: Unit, c: Continuation<Nothing>) {
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
