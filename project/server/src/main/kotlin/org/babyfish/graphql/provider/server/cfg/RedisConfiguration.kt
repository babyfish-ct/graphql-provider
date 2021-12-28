package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.kimmer.Immutable
import kotlin.reflect.KProperty1

class RedisConfiguration<E> {

    fun <T: Immutable> dependsOnReference(prop: KProperty1<E, T>, block: RedisConfiguration<T>.() -> Unit) {

    }

    fun <T: Immutable> dependsOnList(prop: KProperty1<E, List<T>>, block: RedisConfiguration<T>.() -> Unit) {

    }

    fun dependsOn(prop: KProperty1<E, *>) {}
}