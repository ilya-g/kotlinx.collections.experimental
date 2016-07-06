package kotlinx.collections.experimental.benchmarks

import kotlinx.collections.experimental.grouping.*
import org.openjdk.jmh.annotations.*
import java.util.concurrent.atomic.AtomicInteger

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class GroupCount {

    @Param("100000")
    open var elements = 0
    val buckets = 100

    private lateinit var data: List<Element>

    @Setup
    fun createValues() {
        data = generateElements(elements, buckets)
    }


    @Benchmark
    fun countGroup() = data.groupCountBy { it.key }

    @Benchmark
    fun countGrouping() = data.grouping { it.key }.count()

    @Benchmark
    fun countGroupingAtomic() =
            data.grouping { it.key }
                    .fold(AtomicInteger(0)) { acc, e -> acc.apply { incrementAndGet() }}
                    .mapValues { it.value.get() }

    @Benchmark
    fun countGroupingRef() = data.grouping { it.key }.countRef()

    @Benchmark
    fun countGroupingRefInPlace() = data.grouping { it.key }.countRefInPlace()

    @Benchmark
    fun countGroupingReducer() = data.grouping { it.key }.reduce(Count)

    @Benchmark
    fun countGroupingReducerRef() = data.grouping { it.key }.reduce(CountWithRef)


}

