package kotlinx.collections.experimental.benchmarks

import kotlinx.collections.experimental.sequences.benchmarks.*
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.openjdk.jmh.runner.options.TimeValue
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val options = OptionsBuilder()
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.MICROSECONDS)
            .include(SequenceCoroutineOperations::class.java.simpleName + ".map")
            .warmupIterations(6)
            .measurementIterations(20)
            .measurementTime(TimeValue.milliseconds(500))
//            .addProfiler("perfasm")
            .forks(2)

    Runner(options.build()).run()


}
