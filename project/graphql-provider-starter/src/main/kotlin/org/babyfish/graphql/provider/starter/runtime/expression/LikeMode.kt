package org.babyfish.graphql.provider.starter.runtime.expression

enum class LikeMode(
    val startExact: Boolean,
    val endExact: Boolean
) {
    EXACT(true, true),
    START(true, false),
    END(false, true),
    ANYWHERE(false, false)
}