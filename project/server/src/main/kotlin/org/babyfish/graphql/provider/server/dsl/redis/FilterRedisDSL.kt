package org.babyfish.graphql.provider.server.dsl.redis

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.ModelException
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.meta.impl.FilterRedisDependencyImpl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
class FilterRedisDSL internal constructor(
    private val props: MutableSet<KProperty1<out Immutable, *>>
) {

    fun dependsOn(prop: KProperty1<out Immutable, *>) {
        props += prop
    }
}