package org.babyfish.graphql.provider.kimmer

import org.babyfish.graphql.provider.kimmer.jackson.immutableObjectMapper
import org.babyfish.graphql.provider.kimmer.meta.ImmutableProp
import org.babyfish.graphql.provider.kimmer.runtime.DraftSpi
import org.babyfish.graphql.provider.kimmer.runtime.ImmutableSpi
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Immutable {

    companion object {

        @JvmStatic
        fun <T: Immutable> isLoaded(o: T, prop: KProperty1<T, *>): Boolean {
            return (o as ImmutableSpi).`{loaded}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> isLoaded(o: T, prop: ImmutableProp): Boolean {
            return (o as ImmutableSpi).`{loaded}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> get(o: T, prop: KProperty1<T, *>): Any? {
            return (o as ImmutableSpi).`{value}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> get(o: T, prop: ImmutableProp): Any? {
            return (o as ImmutableSpi).`{value}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> shallowHashCode(o: T): Int {
            return (o as ImmutableSpi).hashCode(true)
        }

        @JvmStatic
        fun <T: Immutable> shallowEquals(a: T, b: T): Boolean {
            return (a as ImmutableSpi).equals(b, true)
        }

        @JvmStatic
        fun <T: Immutable> fromString(value: String, type: KClass<T>): T =
            fromString(value, type.java)

        @JvmStatic
        fun <T: Immutable> fromString(value: String, type: Class<T>): T =
            objectMapper.readValue(value, type)
    }
}

interface Connection<N>: Immutable {

    val edges: List<Edge<N>>

    val pageInfo: PageInfo

    interface Edge<N>: Immutable {
        val node: N
        val cursor: String
    }

    interface PageInfo: Immutable {
        val hasNextPage: Boolean
        val hasPreviousPage: Boolean
        val startCursor: String
        val endCursor: String
    }
}

@Target(AnnotationTarget.CLASS)
annotation class Abstract

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class DraftDsl

@DraftDsl
interface Draft<out T: Immutable> {

    companion object {

        @JvmStatic
        fun <T: Immutable, V> set(draft: Draft<T>, prop: KProperty1<T, V>, value: V) {
            (draft as DraftSpi).`{value}`(prop.name, value)
        }

        @JvmStatic
        fun set(draft: Draft<*>, prop: ImmutableProp, value: Any?) {
            (draft as DraftSpi).`{value}`(prop.name, value)
        }

        @JvmStatic
        fun <T: Immutable> unload(draft: Draft<T>, prop: KProperty1<T, *>) {
            (draft as DraftSpi).`{unload}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> unload(draft: Draft<T>, prop: ImmutableProp) {
            (draft as DraftSpi).`{unload}`(prop.name)
        }
    }
}

interface SyncDraft<out T: Immutable>: Draft<T> {
    fun <X: Immutable> new(type: KClass<X>): SyncDraftCreator<X> =
        SyncDraftCreator(type)
}

interface AsyncDraft<out T: Immutable>: Draft<T> {
    fun <X: Immutable> newAsync(type: KClass<X>) =
        AsyncDraftCreator(type)
}

private val objectMapper = immutableObjectMapper()

