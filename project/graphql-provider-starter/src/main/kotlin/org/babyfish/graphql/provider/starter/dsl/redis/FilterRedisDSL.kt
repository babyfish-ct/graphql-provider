package org.babyfish.graphql.provider.starter.dsl.redis

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
class FilterRedisDSL internal constructor(
    private val props: MutableSet<KProperty1<out Immutable, *>>
) {

    fun dependsOn(prop: KProperty1<out Immutable, *>) {
        props += prop
    }
}