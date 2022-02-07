package org.babyfish.graphql.provider.starter.runtime.query

@JvmInline
value class TableAliasAllocator(
    private val idRef: IntArray = IntArray(1)
) {
    fun allocate(): String =
        "table_${++idRef[0]}"
}