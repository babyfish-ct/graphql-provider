package org.babyfish.graphql.provider.kimmer

import kotlinx.coroutines.delay
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
        fun <T: Immutable> getThrowable(o: T, prop: KProperty1<T, *>): Throwable? {
            return (o as ImmutableSpi).`{throwable}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> get(o: T, prop: KProperty1<T, *>): Any? {
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

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class DraftDsl

@DraftDsl
interface Draft<out T: Immutable>

interface SyncDraft<out T: Immutable>: Draft<T> {

    fun <X: Any, D: SyncDraft<X>> new(
        draftType: KClass<D>,
        base: X? = null,
        block: D.() -> Unit
    ): D
}

@DraftDsl
interface AsyncDraft<out T: Immutable>: Draft<T> {

    suspend fun <X: Any, D: AsyncDraft<X>> new(
        draftType: KClass<D>,
        base: X? = null,
        block: suspend D.() -> Unit
    ): D
}

fun <T: Any, D: SyncDraft<T>> new(
    draftType: KClass<D>,
    base: T? = null,
    block: D.() -> Unit
): T {
    TODO()
}

suspend fun <T: Any, D: AsyncDraft<T>> new(
    draftType: KClass<D>,
    base: T? = null,
    block: suspend D.() -> Unit
): T {

    delay(1000)
    TODO()
}
