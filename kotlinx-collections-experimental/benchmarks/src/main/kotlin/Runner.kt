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
            .include("SequenceBuilders[123]")
            .warmupIterations(6)
            .measurementIterations(15)
            .measurementTime(TimeValue.milliseconds(500))
//            .jvm("""c:\Program Files\Java\jre1.8.0_101\bin\java.exe""")
//            .jvmArgsAppend("-XX:TieredStopAtLevel=1")
//            .jvmArgsAppend("-Xint")
//            .addProfiler("perfasm")
            .forks(2)

    Runner(options.build()).run()


}
