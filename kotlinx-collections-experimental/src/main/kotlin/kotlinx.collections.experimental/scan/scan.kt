package kotlinx.collections.experimental.scan

/**
 * Accumulates value starting with [initial] value and applying [operation] from left to right to current accumulator value and each element.
 */
public inline fun <T, R> Iterable<T>.scan(initial: R, operation: (R, T) -> R): List<R> {
    val result = mutableListOf<R>()
    var accumulator = initial
    for (element in this) {
        accumulator = operation(accumulator, element)
        result.add(accumulator)
    }
    return result
}

public fun <T, R> Sequence<T>.scan(initial: R, operation: (R, T) -> R): Sequence<R> = Sequence {
    object : AbstractIterator<R>() {
        var accumulator = initial
        val iterator = this@scan.iterator()
        override fun computeNext() {
            if (!iterator.hasNext())
                done()
            else {
                val element = iterator.next()
                accumulator = operation(accumulator, element)
                setNext(accumulator)
            }
        }
    }
}

fun main(args: Array<String>) {
    val values = listOf("apple", "fooz", "bisquit", "abc", "far", "bar", "foo")

    val scanResult = values.scan("") { acc, e -> acc + "/" + e }
    println(scanResult)
    assert(scanResult == values.asSequence().scan("") { acc, e -> acc + "/" + e }.toList())

}