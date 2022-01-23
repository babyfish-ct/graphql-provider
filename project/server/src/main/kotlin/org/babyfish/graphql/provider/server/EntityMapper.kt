package org.babyfish.graphql.provider.server

import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.dsl.FilterDSL
import org.babyfish.graphql.provider.server.dsl.UserImplementationDSL
import kotlin.reflect.KProperty1

abstract class EntityMapper<E: Immutable> {

    fun map() {}

    abstract fun EntityTypeDSL<E>.map()

    protected fun <X: Immutable> filterList(
        prop: KProperty1<E, List<X>?>,
        block: FilterDSL<X>.() -> Unit
    ) {

    }

    protected fun <X: Immutable> filterConnection(
        prop: KProperty1<E, out Collection<X>?>,
        block: FilterDSL<X>.() -> Unit
    ) {
    }

    protected fun <T> userImplementation(
        prop: KProperty1<E, T?>,
        block: UserImplementationDSL<E, T>.() -> Unit
    ) {

    }
}