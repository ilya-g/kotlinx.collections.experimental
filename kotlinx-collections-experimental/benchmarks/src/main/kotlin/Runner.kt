package kotlinx.collections.experimental.benchmarks

import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.openjdk.jmh.runner.options.TimeValue
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val options = OptionsBuilder()
            .mode(Mode.AverageTime)
            .include(PrimitiveCount::class.java.simpleName)
            .warmupIterations(6)
            .measurementIterations(15)
            .measurementTime(TimeValue.milliseconds(500))
            .forks(2)
            .timeUnit(TimeUnit.MICROSECONDS)

    Runner(options.build()).run()


}
