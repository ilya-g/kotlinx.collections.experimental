package kotlinx.collections.experimental.benchmarks

import kotlinx.collections.experimental.grouping.*
import org.openjdk.jmh.annotations.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class PrimitiveCount {

    @Param("100", "100000")
    open var elements = 0
    val buckets = 100

    private lateinit var data: IntArray

    @Setup
    fun createValues() {
        val rnd = Random()
        data = IntArray(elements) { rnd.nextInt() }
    }

    fun key(value: Int): Int = value % buckets


    @Benchmark
    fun countGroup() = data.groupCountBy { key(it) }

    @Benchmark
    fun countGrouping() = data.groupingBy { key(it) }.eachCount()

    @Benchmark
    fun countGroupingRefInPlace() = data.groupingBy { key(it) }.eachCountRefInPlace()



}

