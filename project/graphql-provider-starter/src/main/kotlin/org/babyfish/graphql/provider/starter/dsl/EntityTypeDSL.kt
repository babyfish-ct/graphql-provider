package org.babyfish.graphql.provider.starter.dsl

import org.babyfish.graphql.provider.starter.dsl.db.EntityTypeDatabaseDSL
import org.babyfish.graphql.provider.starter.dsl.graphql.EntityTypeGraphQLDSL
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.starter.meta.impl.EntityTypeImpl
import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
class EntityTypeDSL<E: Immutable> internal constructor(
    private val entityType: EntityTypeImpl
) {

    fun db(block: EntityTypeDatabaseDSL.() -> Unit) {
        EntityTypeDatabaseDSL(this.entityType.database).block()
    }

    fun redis(enabled: Boolean) {
        this.entityType.redis.enabled = enabled
    }

    fun graphql(block: EntityTypeGraphQLDSL.() -> Unit) {
        EntityTypeGraphQLDSL(this.entityType.graphql).block()
    }

    fun <T> id(
        prop: KProperty1<E, T>,
        block: (IdDSL<T>.() -> Unit)? = null
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, prop, true)
        block?.let {
            IdDSL<T>(entityProp).it()
        }
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T> scalar(
        prop: KProperty1<E, T>,
        block: ScalarDSL<T>.() -> Unit
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, prop)
        ScalarDSL<T>(entityProp).block()
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> reference(
        prop: KProperty1<E, T?>,
        block: ReferenceDSL.() -> Unit
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, prop)
        ReferenceDSL(entityProp).block()
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> list(
        prop: KProperty1<E, List<T>>,
        block: CollectionDSL<T>.() -> Unit
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, prop)
        CollectionDSL<T>(entityProp).block()
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> connection(
        prop: KProperty1<E, out Connection<T>>,
        block: CollectionDSL<T>.() -> Unit
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, prop)
        CollectionDSL<T>(entityProp).block()
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> mappedReference(
        prop: KProperty1<E, T>,
        mappedBy: KProperty1<T, *>
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, prop)
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> mappedList(
        prop: KProperty1<E, List<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedCollectionDSL<T>.() -> Unit)? = null
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, prop, mappedBy = mappedBy)
        block?.let {
            MappedCollectionDSL<T>(entityProp).it()
        }
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> mappedConnection(
        prop: KProperty1<E, out Connection<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedCollectionDSL<T>.() -> Unit)? = null
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, prop, mappedBy = mappedBy)
        block?.let {
            MappedCollectionDSL<T>(entityProp).it()
        }
        entityType.declaredProps[prop.name] = entityProp
    }

    private fun validateProp(prop: KProperty1<*, *>) {
        entityType.declaredProps[prop.name]?.let {
            if (it.kotlinProp === prop) {
                throw ModelException("'$prop' cannot be configured twice")
            } else {
                throw ModelException("Conflict properties: '${prop}' and '$it'")
            }
        }
    }
}