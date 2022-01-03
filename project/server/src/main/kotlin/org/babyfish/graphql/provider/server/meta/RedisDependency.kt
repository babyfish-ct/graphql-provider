package org.babyfish.graphql.provider.server.meta

import kotlin.reflect.KProperty1

interface RedisDependency {

    val category: Category

    val prop: KProperty1<*, *>

    val dependencies: List<RedisDependency>

    enum class Category {
        REFERENCE,
        LIST,
        SCALAR
    }
}