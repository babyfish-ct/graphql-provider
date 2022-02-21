package org.babyfish.graphql.provider.meta

enum class CacheLevel {
    NO_CACHE,
    FOREVER_UNTIL_TIMEOUT,
    EVICTED_BEFORE_MUTATION,
    PROTECTED_BY_LOCK
}