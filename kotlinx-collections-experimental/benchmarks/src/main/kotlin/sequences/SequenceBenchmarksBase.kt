package kotlinx.collections.experimental.sequences.benchmarks

import org.openjdk.jmh.infra.Blackhole

open class SequenceBenchmarksBase {



    val elements = 100000
    val sequences = listOf(
        (1..elements).asSequence(),   // Iterable.asSequence
        (0..elements).asSequence().drop(1),  // DropSequence
        Array(elements) { it + 1 }.asSequence()  // Array.asSequence
    ).take(3)

    private var seq_impl_id = 0

    val sequence: Sequence<Int>
        get() = sequences[(seq_impl_id++) % sequences.size]


}

fun <T> Sequence<T>.consume(blackhole: Blackhole) = forEach { blackhole.consume(it) }
