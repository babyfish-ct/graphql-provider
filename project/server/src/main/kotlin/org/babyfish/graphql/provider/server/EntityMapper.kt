package org.babyfish.graphql.provider.server

import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.dsl.FilterDSL
import org.babyfish.graphql.provider.server.dsl.UserImplementationDSL
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.meta.ImmutableType
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.KProperty1

abstract class EntityMapper<E: Immutable> {

    val immutableType: ImmutableType

    init {
        val entityJavaType =
            GenericTypeResolver
                .resolveTypeArgument(this::class.java, EntityMapper::class.java)
                ?: throw ModelException(
                    "Illegal class '${this::class.qualifiedName}', " +
                        "type argument of '${EntityMapper::class.qualifiedName}' is not specified"
                )
        if (entityJavaType.simpleName == "Query") {
            throw ModelException(
                "Illegal class'${this::class.qualifiedName}', " +
                    "type argument of '${EntityMapper::class.qualifiedName}' " +
                    "cannot be specified as 'Query'"
            )
        }
        if (!entityJavaType.isInterface && !Immutable::class.java.isAssignableFrom(entityJavaType)) {
            throw ModelException(
                "Illegal class'${this::class.qualifiedName}', " +
                    "type argument of '${EntityMapper::class.qualifiedName}' " +
                    "must be specified as derived interface of '${Immutable::class.qualifiedName}'"
            )
        }
        if (Connection::class.java.isAssignableFrom(entityJavaType)) {
            throw ModelException(
                "Illegal class'${this::class.qualifiedName}', " +
                    "type argument of '${EntityMapper::class.qualifiedName}' " +
                    "cannot be specified as derived interface of '${Connection::class.qualifiedName}'"
            )
        }
        immutableType = ImmutableType.of(entityJavaType as Class<out Immutable>)
    }

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