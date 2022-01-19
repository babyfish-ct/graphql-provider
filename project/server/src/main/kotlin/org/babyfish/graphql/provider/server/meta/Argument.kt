package org.babyfish.graphql.provider.server.meta

import kotlin.reflect.KClass

class Argument<T> internal constructor(
    val name: String,
    val type: KClass<*>,
    val list: Boolean = false,
    val nullable: Boolean = false
)
