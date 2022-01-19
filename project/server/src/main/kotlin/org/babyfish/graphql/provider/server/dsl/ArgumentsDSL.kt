package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.meta.Argument
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass

@GraphQLProviderDSL
abstract class ArgumentsDSL<E: Immutable> internal constructor(
    val entityProp: EntityPropImpl
) {
    fun <T: Any> argument(
        name: String,
        type: KClass<T>,
        block: (FilterDSL<E>.(T) -> Unit)? = null
    ): Argument<T> {
        return Argument(
            name,
            type
        )
    }

    fun <T: Any> optionalArgument(
        name: String,
        type: KClass<T>,
        block: (FilterDSL<E>.(T) -> Unit)? = null
    ): Argument<T?> {
        return Argument(
            name,
            type,
            nullable = true
        )
    }

    fun <T: Any> listArgument(
        name: String,
        elementType: KClass<T>,
        block: (FilterDSL<E>.(List<T>) -> Unit)? = null
    ): Argument<List<T>> {
        return Argument(
            name,
            elementType,
            list = true
        )
    }

    fun <T: Any> optionalListArgument(
        name: String,
        elementType: KClass<T>,
        block: (FilterDSL<E>.(List<T>) -> Unit)? = null
    ): Argument<List<T>?> {
        return Argument(
            name,
            elementType,
            list = true
        )
    }
}