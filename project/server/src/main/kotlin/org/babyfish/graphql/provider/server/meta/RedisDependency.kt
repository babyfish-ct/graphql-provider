package org.babyfish.graphql.provider.server.meta

interface RedisDependency {

    val category: Category

    val prop: EntityProp

    val dependencies: Collection<RedisDependency>

    enum class Category {
        SCALAR,
        REFERENCE,
        LIST,
        CONNECTION
    }
}