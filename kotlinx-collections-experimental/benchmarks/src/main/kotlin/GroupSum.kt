package kotlinx.collections.experimental.benchmarks

import kotlinx.collections.experimental.grouping.*
import org.openjdk.jmh.annotations.*
import java.util.concurrent.atomic.AtomicInteger

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class GroupSum {

    @Param("100000")
    open var elements = 0
    val buckets = 100

    private lateinit var data: List<Element>

    @Setup
    fun createValues() {
        data = generateElements(elements, buckets)
    }

    @Benchmark
    fun naive() = data.groupBy { it.key }.mapValues { it.value.sumBy { it.value } }

    @Benchmark
    fun sumGroup() = data.groupBySumBy(keySelector = {it.key}, valueSelector = {it.value})

    @Benchmark
    fun sumGrouping() = data.groupingBy { it.key }.sumEachBy { it.value }

    @Benchmark
    fun sumGroupingAtomic() = data.groupingBy { it.key }
            .fold(AtomicInteger()) { acc, e -> acc.apply { addAndGet(e.value) }}
            .mapValues { it.value.get() }

    @Benchmark
    fun sumGroupingRef() = data.groupingBy { it.key }.sumEachByRef { it.value }

    @Benchmark
    fun sumGroupingRefInPlace() = data.groupingBy { it.key }.sumEachByRefInPlace { it.value }

    @Benchmark
    fun sumGroupingReducer() = data.groupingBy { it.key }.reduce(Sum.by { it.value })

    @Benchmark
    fun sumGroupingReducerRef() = data.groupingBy { it.key }.reduce(RefSum.by { it.value })

}

