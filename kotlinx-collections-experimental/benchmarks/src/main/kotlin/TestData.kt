package kotlinx.collections.experimental.benchmarks

import kotlinx.collections.experimental.grouping.groupCountBy
import java.util.*


data class Element(val key: String, val value: Int)

fun generateElements(count: Int, keys: Int): List<Element> {
    val keyChars = ('a'..'z').toList()
    fun key(value: Int) = buildString {
        var v = value % keys
        while (v != 0) {
            append(keyChars[v % keyChars.size])
            v /= keyChars.size
        }
    }
    val rnd = Random()

    val result = (0 until count).map { Element(key(rnd.nextInt(count)), rnd.nextInt(count)) }
    println(result.groupCountBy { it.key }.entries.sortedByDescending { it.value }.take(10))
    return result
}