package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.AbstractQuery
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
class QueryDSL<Q: AbstractQuery> internal constructor() {

    fun scalar(prop: KProperty1<Q, *>) {

    }

    fun reference(
        prop: KProperty1<Q, *>
    ) {

    }

    fun <E: Immutable> list(
        prop: KProperty1<Q, List<E>>,
        block: PhantomCollectionDSL<E>.() -> Unit
    ) {

    }

    fun <E: Immutable, C: Connection<E>> connection(
        prop: KProperty1<Q, C>,
        block: PhantomCollectionDSL<E>.() -> Unit
    ) {

    }
}