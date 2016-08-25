package kotlinx.collections.experimental.sequences.benchmarks

import kotlinx.collections.experimental.sequences.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole


@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class SequenceMapFilterFusionOperations : SequenceBenchmarksBase() {

    @Benchmark fun map_filter_std(blackhole: Blackhole) {
        sequence.map_std { if (it % 3 == 0) it else null }.filter_std { it != null }.consume(blackhole)
    }
    @Benchmark fun map_filter_fused(blackhole: Blackhole) {
        sequence.map_fused { if (it % 3 == 0) it else null }.filter_fused { it != null }.consume(blackhole)
    }


    @Benchmark fun map_map_filter_std(blackhole: Blackhole) {
        sequence.map_std { it / 100 }.map_std { if (it % 3 == 0) it else null }.filter_std { it != null }.consume(blackhole)
    }
    @Benchmark fun map_map_filter_fused(blackhole: Blackhole) {
        sequence.map_fused { it / 100 }.map_fused { if (it % 3 == 0) it else null }.filter_fused { it != null }.consume(blackhole)
    }

}