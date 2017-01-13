package kotlinx.collections.experimental.sequences.benchmarks

import kotlinx.collections.experimental.sequences.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

abstract class SequenceBuildersBase {
    val data = (1..25).toList()

    abstract fun <T> buildSequence(builderAction: suspend SequenceBuilder<T>.() -> Unit): Sequence<T>

/*
    @Benchmark fun baseLine(blackhole: Blackhole) {
        val seq = data.asSequence()
        seq.consume(blackhole)
        seq.consume(blackhole)
        seq.consume(blackhole)
    }
*/

    @Benchmark fun onlyYield(blackhole: Blackhole) {
        buildSequence {
            for (element in data)
                yield(element)
            for (element in data)
                yield(element)
            for (element in data)
                yield(element)
        }.consume(blackhole)
    }

    @Benchmark fun onlyYieldAll(blackhole: Blackhole) {
        buildSequence {
            yieldAll(data)
            yieldAll(data)
            yieldAll(data)
        }.consume(blackhole)
    }

    @Benchmark fun mixedYields(blackhole: Blackhole) {
        buildSequence {
            for (element in data)
                yield(element)
            yieldAll(data)
            for (element in data)
                yield(element)
        }.consume(blackhole)
    }

}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class SequenceBuilders1 : SequenceBuildersBase() {
    override fun <T> buildSequence(builderAction: suspend SequenceBuilder<T>.() -> Unit): Sequence<T> =
            buildSequence1(builderAction)
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class SequenceBuilders2 : SequenceBuildersBase() {
    override fun <T> buildSequence(builderAction: suspend SequenceBuilder<T>.() -> Unit): Sequence<T> =
            buildSequence2(builderAction)
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class SequenceBuilders3 : SequenceBuildersBase() {
    override fun <T> buildSequence(builderAction: suspend SequenceBuilder<T>.() -> Unit): Sequence<T> =
            buildSequence3(builderAction)
}

