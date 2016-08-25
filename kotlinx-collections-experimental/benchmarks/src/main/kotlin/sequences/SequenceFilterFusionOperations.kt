package kotlinx.collections.experimental.sequences.benchmarks

import kotlinx.collections.experimental.sequences.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole


@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class SequenceFilterFusionOperations : SequenceBenchmarksBase() {

    @Benchmark fun triple_filter_std(blackhole: Blackhole) {
        sequence.filter_std { it % 3 == 0 }.filter_std { it % 2 == 0 }.filter_std { it % 5 == 0 }.consume(blackhole)
    }
    @Benchmark fun triple_filter_std_arr(blackhole: Blackhole) {
        sequence.filter_std_arr { it % 3 == 0 }.filter_std_arr { it % 2 == 0 }.filter_std_arr { it % 5 == 0 }.consume(blackhole)
    }
    @Benchmark fun triple_filter_fused(blackhole: Blackhole) {
        sequence.filter_fused { it % 3 == 0 }.filter_fused { it % 2 == 0 }.filter_fused { it % 5 == 0 }.consume(blackhole)
    }

    @Benchmark fun double_filter_std(blackhole: Blackhole) {
        sequence.filter_std { it % 3 == 0 }.filter_std { it % 2 == 0 }.consume(blackhole)
    }
    @Benchmark fun double_filter_std_arr(blackhole: Blackhole) {
        sequence.filter_std_arr { it % 3 == 0 }.filter_std_arr { it % 2 == 0 }.consume(blackhole)
    }
    @Benchmark fun double_filter_fused(blackhole: Blackhole) {
        sequence.filter_fused { it % 3 == 0 }.filter_fused { it % 2 == 0 }.consume(blackhole)
    }

    @Benchmark fun single_filter_std(blackhole: Blackhole) {
        sequence.filter_std { it % 3 == 0 && it % 2 == 0 && it % 5 == 0 }.consume(blackhole)
    }


}