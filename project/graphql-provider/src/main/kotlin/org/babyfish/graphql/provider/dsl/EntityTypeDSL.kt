package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.db.EntityTypeDatabaseDSL
import org.babyfish.graphql.provider.dsl.graphql.EntityTypeGraphQLDSL
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.meta.impl.ModelTypeImpl
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.graphql.provider.runtime.FakeID
import org.babyfish.kimmer.jackson.immutableObjectMapper
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.EntityMappingBuilder
import org.babyfish.kimmer.sql.spi.databaseIdentifier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
class EntityTypeDSL<E: Entity<ID>, ID: Comparable<ID>> internal constructor(
    private val modelType: ModelTypeImpl,
    private val builder: EntityMappingBuilder
) {
    fun db(block: EntityTypeDatabaseDSL<E, ID>.() -> Unit) {
        val dsl = EntityTypeDatabaseDSL<E, ID>().apply {
            block()
        }

        builder.entity(
            modelType.kotlinType,
            tableName = dsl.tableName ?: databaseIdentifier(modelType.kotlinType.simpleName!!),
            idGenerator = dsl._idGenerator,
            versionProp = dsl.versionProp
        )
    }

    fun graphql(block: EntityTypeGraphQLDSL.() -> Unit) {
        val graphql = EntityTypeGraphQLDSL(modelType.kotlinType.simpleName!!).run {
            block()
            create()
        }
        modelType.graphql = graphql
    }

    fun <T> scalar(
        prop: KProperty1<E, T>,
        block: ScalarDSL<E, ID, T>.() -> Unit
    ) {
        val modelProp = builder.prop(prop) as ModelPropImpl
        if (modelProp.targetType !== null) {
            throw ModelException("Cannot map '${prop}' as scalar property")
        }
        ScalarDSL(prop).run {
            block()
            create()
        }?.let {
            builder.storage(prop, it)
        }
    }

    fun <T: Entity<*>> reference(
        prop: KProperty1<E, T?>,
        block: (ReferenceDSL.() -> Unit)? = null
    ) {
        val modelProp = builder.prop(prop) as ModelPropImpl
        if (!modelProp.isReference) {
            throw ModelException("Cannot map '${prop}' as reference property")
        }
        ReferenceDSL(modelProp).run {
            if (block !== null) {
                block()
            }
            create()
        }.let {
            builder.storage(prop, it)
        }
    }

    fun <T: Entity<*>> list(
        prop: KProperty1<E, List<T>>,
        block: CollectionDSL.() -> Unit
    ) {
        val modelProp = builder.prop(prop) as ModelPropImpl
        if (!modelProp.isList) {
            throw ModelException("Cannot map '${prop}' as list property")
        }
        CollectionDSL(modelProp).run {
            block()
            create()
        }.let {
            builder.storage(prop, it)
        }
    }

    fun <T: Entity<*>> connection(
        prop: KProperty1<E, out Connection<T>>,
        block: CollectionDSL.() -> Unit
    ) {
        val modelProp = builder.prop(prop) as ModelPropImpl
        if (!modelProp.isConnection) {
            throw ModelException("Cannot map '${prop}' as list property")
        }
        CollectionDSL(modelProp).run {
            block()
            create()
        }.let {
            builder.storage(prop, it)
        }
    }

    fun <T: Entity<*>> mappedReference(
        prop: KProperty1<E, T>,
        mappedBy: KProperty1<T, *>
    ) {
        val modelProp = builder.inverseProp(prop, mappedBy) as ModelPropImpl
        if (!modelProp.isReference) {
            throw ModelException("Cannot map '${prop}' as mapped reference property")
        }
    }

    fun <T: Entity<*>> mappedList(
        prop: KProperty1<E, List<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedCollectionDSL<T>.() -> Unit)? = null
    ) {
        val modelProp = builder.inverseProp(prop, mappedBy) as ModelPropImpl
        if (!modelProp.isList) {
            throw ModelException("Cannot map '${prop}' as mapped list property")
        }
    }

    fun <T: Entity<*>> mappedConnection(
        prop: KProperty1<E, out Connection<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedCollectionDSL<T>.() -> Unit)? = null
    ) {
        val modelProp = builder.inverseProp(prop, mappedBy) as ModelPropImpl
        if (!modelProp.isReference) {
            throw ModelException("Cannot map '${prop}' as mapped connection property")
        }
    }

    fun <T> userImplementation(
        prop: KProperty1<E, T>
    ) {
        //builder.transientProp(prop)
    }
}
