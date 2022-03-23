package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import java.util.*
import kotlin.math.max

internal suspend fun DataFetchingEnvironment.limit(
    countOnce: AsyncOnce<Int>
): Pair<Int, Int> {
    val first = arguments["first"] as Int?
    val after = arguments["after"] as String?
    val last = arguments["last"] as Int?
    val before = arguments["before"] as String?
    var (start, endExclusive) = when {
        first !== null || after !== null -> {
            if (last !== null || before !== null) {
                throw IllegalArgumentException(
                    "'last' or 'before' cannot be specified " +
                        "when 'first' or 'after' is specified"
                )
            }
            if (first === null) {
                throw IllegalArgumentException(
                    "'first' must be specified " +
                        "when 'after' is specified"
                )
            }
            if (first < 0) {
                throw IllegalArgumentException("'first' cannot be negative number")
            }
            val startNo = after?.let {
                cursorToIndex(it) + 1
            } ?: 0
            startNo to startNo + first
        }
        last !== null || before !== null -> {
            if (last === null) {
                throw IllegalArgumentException(
                    "'last' must be specified " +
                        "when 'before' is specified"
                )
            }
            if (last < 0) {
                throw IllegalArgumentException("'last' cannot be negative number")
            }
            val endNo = before?.let {
                cursorToIndex(it)
            } ?: countOnce.get()
            endNo - last to endNo
        }
        else ->
            throw IllegalArgumentException("neither 'first' nor 'last' is specified")
    }
    if (start < 0) {
        start = 0
    }
    if (endExclusive > countOnce.get()) {
        endExclusive = countOnce.get()
    }
    return max(endExclusive - start, 0) to start
}

internal class AsyncOnce<T: Any>(
    private val supplier: suspend () -> T
) {
    private var value: T? = null

    suspend fun get(): T =
        value ?: supplier().also {
            value = it
        }
}

internal fun indexToCursor(index: Int): String =
    Base64.getEncoder().encodeToString(index.toString().toByteArray())

internal fun cursorToIndex(cursor: String): Int =
    String(Base64.getDecoder().decode(cursor)).toInt()