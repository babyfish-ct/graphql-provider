package org.babyfish.graphql.provider

import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.dsl.UserImplementationDSL
import org.babyfish.graphql.provider.runtime.filterExecutionContext
import org.babyfish.graphql.provider.runtime.registerEntityField
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.KProperty1

abstract class EntityMapper<E: Entity<ID>, ID: Comparable<ID>> {

    val immutableType: ImmutableType

    init {
        val entityJavaType =
            GenericTypeResolver
                .resolveTypeArgument(this::class.java, org.babyfish.graphql.provider.EntityMapper::class.java)
                ?: throw org.babyfish.graphql.provider.ModelException(
                    "Illegal class '${this::class.qualifiedName}', " +
                        "type argument of '${org.babyfish.graphql.provider.EntityMapper::class.qualifiedName}' is not specified"
                )
        if (entityJavaType.simpleName == "Query") {
            throw org.babyfish.graphql.provider.ModelException(
                "Illegal class'${this::class.qualifiedName}', " +
                    "type argument of '${org.babyfish.graphql.provider.EntityMapper::class.qualifiedName}' " +
                    "cannot be specified as 'Query'"
            )
        }
        if (!entityJavaType.isInterface && !Entity::class.java.isAssignableFrom(entityJavaType)) {
            throw org.babyfish.graphql.provider.ModelException(
                "Illegal class'${this::class.qualifiedName}', " +
                    "type argument of '${org.babyfish.graphql.provider.EntityMapper::class.qualifiedName}' " +
                    "must be specified as derived interface of '${Entity::class.qualifiedName}'"
            )
        }
        if (Connection::class.java.isAssignableFrom(entityJavaType)) {
            throw org.babyfish.graphql.provider.ModelException(
                "Illegal class'${this::class.qualifiedName}', " +
                    "type argument of '${org.babyfish.graphql.provider.EntityMapper::class.qualifiedName}' " +
                    "cannot be specified as derived interface of '${Connection::class.qualifiedName}'"
            )
        }
        immutableType = ImmutableType.of(entityJavaType as Class<out Entity<*>>)
    }

    abstract fun EntityTypeDSL<E, ID>.config()

    protected fun <X: Entity<XID>, XID: Comparable<XID>> filterList(
        prop: KProperty1<E, List<X>?>,
        block: FilterDSL<X, XID>.() -> Unit
    ) {
        if (!registerEntityField(prop, this)) {
            FilterDSL<X, XID>(filterExecutionContext).block()
        }
    }

    protected fun <X: Entity<XID>, XID: Comparable<XID>> filterConnection(
        prop: KProperty1<E, out Collection<X>?>,
        block: FilterDSL<X, XID>.() -> Unit
    ) {
        if (!registerEntityField(prop, this)) {
            FilterDSL<X, XID>(filterExecutionContext).block()
        }
    }

    protected fun <T> userImplementation(
        prop: KProperty1<E, T?>,
        block: UserImplementationDSL<E, T>.() -> Unit
    ) {

    }
}