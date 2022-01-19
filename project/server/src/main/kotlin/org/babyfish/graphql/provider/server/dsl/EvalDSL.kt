package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.meta.Argument

@GraphQLProviderDSL
open class EvalDSL internal constructor() {

    inline fun <T> Argument<T>.eval(): T =
        TODO()

    inline fun <T: Any> Argument<T?>.eval(defaultValue: T): T =
        eval() ?: defaultValue

    inline fun <T: Any> Argument<T?>.eval(defaultValueSupplier: () -> T): T =
        eval() ?: defaultValueSupplier()
}