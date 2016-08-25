package kotlinx.collections.experimental.sequences.benchmarks

import kotlinx.collections.experimental.sequences.map_fused
import kotlinx.collections.experimental.sequences.map_std
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole


@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class SequenceMapFusionOperations : SequenceBenchmarksBase() {

    @Benchmark fun map_map_std(blackhole: Blackhole) {
        sequence.map_std { it / 3 }.map_std { it / 2 }.consume(blackhole)
    }
    @Benchmark fun map_map_fused(blackhole: Blackhole) {
        sequence.map_fused { it / 3 }.map_fused { it / 2 }.consume(blackhole)
    }

}

