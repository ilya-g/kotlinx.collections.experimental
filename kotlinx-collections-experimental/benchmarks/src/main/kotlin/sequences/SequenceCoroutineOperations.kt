package kotlinx.collections.experimental.sequences.benchmarks

import kotlinx.collections.experimental.sequences.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole


@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class SequenceCoroutineOperations : SequenceBenchmarksBase() {

    var counter = 0

    @Benchmark fun filter_std(blackhole: Blackhole) {
        sequence.filter_std { it % 3 == 0 }.consume(blackhole)
    }
    @Benchmark fun filter_std_ai(blackhole: Blackhole) {
        sequence.filter_std_ai { it % 3 == 0 }.consume(blackhole)
    }
    @Benchmark fun filter_ai(blackhole: Blackhole) {
        sequence.filter_ai { it % 3 == 0 }.consume(blackhole)
    }
    @Benchmark fun filter_c(blackhole: Blackhole) {
        sequence.filter_c { it % 3 == 0 }.consume(blackhole)
    }
    @Benchmark fun filter_ci(blackhole: Blackhole) {
        sequence.filter_ci { it % 3 == 0 }.consume(blackhole)
    }

    @Benchmark fun map(blackhole: Blackhole) {
        val sequence = when (counter++ % 3) {
            0 -> sequence.map { it % 3 }
            1 -> sequence.map { it % 5 }
            else -> sequence.map { it + 10 }
        }
        sequence.consume(blackhole)
    }
    @Benchmark fun map_c(blackhole: Blackhole) {
        val sequence = when (counter++ % 3) {
            0 -> sequence.map_c { it % 3 }
            1 -> sequence.map_c { it % 5 }
            else -> sequence.map_c { it + 10 }
        }
        sequence.consume(blackhole)
    }
    @Benchmark fun map_ci(blackhole: Blackhole) {
        val sequence = when (counter++ % 3) {
            0 -> sequence.map_ci { it % 3 }
            1 -> sequence.map_ci { it % 5 }
            else -> sequence.map_ci { it + 10 }
        }
        sequence.consume(blackhole)
    }

}