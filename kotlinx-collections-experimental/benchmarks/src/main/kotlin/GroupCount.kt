package kotlinx.collections.experimental.benchmarks

import kotlinx.collections.experimental.grouping.*
import org.openjdk.jmh.annotations.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class GroupCount {

    @Param("100", "100000")
    open var elements = 0
    val buckets = 100

    private lateinit var data: List<Element>

    @Setup
    fun createValues() {
        data = generateElements(elements, buckets)
    }


    @Benchmark
    fun naive() = data.groupBy { it.key }.mapValues { it.value.size }

    @Benchmark
    fun countGroup() = data.groupCountBy { it.key }

    @Benchmark
    fun countGrouping() = data.groupingBy { it.key }.eachCount()

    @Benchmark
    fun countGroupingAtomic() =
            data.groupingBy { it.key }
                    .fold(AtomicInteger(0)) { acc, e -> acc.apply { incrementAndGet() }}
                    .mapValues { it.value.get() }

    @Benchmark
    fun countGroupingRef() = data.groupingBy { it.key }.eachCountRef()

    @Benchmark
    fun countGroupingRefInPlace() = data.groupingBy { it.key }.eachCountRefInPlace()

    @Benchmark
    fun countGroupingReducer() = data.groupingBy { it.key }.reduce(Count)

    @Benchmark
    fun countGroupingReducerRef() = data.groupingBy { it.key }.reduce(CountWithRef)

    @Benchmark
    fun countGroupingCollector() = data.groupingBy { it.key }.eachCollect(Collectors.counting())


}

