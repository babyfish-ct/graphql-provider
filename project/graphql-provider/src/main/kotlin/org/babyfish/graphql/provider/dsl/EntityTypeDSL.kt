package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.db.EntityTypeDatabaseDSL
import org.babyfish.graphql.provider.dsl.graphql.EntityTypeGraphQLDSL
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.graphql.provider.meta.impl.ModelTypeImpl
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.graphql.UserImplementationPropGraphQLDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.EntityMappingBuilder
import org.babyfish.kimmer.sql.spi.databaseIdentifier
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
        val defaultName = modelType.kotlinType.simpleName!!
        val graphql = EntityTypeGraphQLDSL().run {
            block()
            graphql(defaultName)
        }
        modelType.setGraphQL(graphql)
    }

    fun <T> scalar(
        prop: KProperty1<E, T>,
        block: (ScalarDSL<E, ID, T>.() -> Unit)? = null
    ) {
        val modelProp = builder.prop(prop) as ModelPropImpl
        if (modelProp.targetType !== null) {
            throw ModelException("Cannot map '${prop}' as scalar property")
        }
        ScalarDSL<E, ID, T>(modelProp).run {
            if (block !== null) {
                block()
            }
            storage()
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
            storage()
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
            storage()
        }.let {
            builder.storage(prop, it)
        }
    }

    fun <T: Entity<*>> connection(
        prop: KProperty1<E, Connection<T>>,
        block: CollectionDSL.() -> Unit
    ) {
        val modelProp = builder.prop(prop) as ModelPropImpl
        if (!modelProp.isConnection) {
            throw ModelException("Cannot map '${prop}' as list property")
        }
        CollectionDSL(modelProp).run {
            block()
            storage()
        }.let {
            builder.storage(prop, it)
        }
    }

    fun <T: Entity<*>> mappedReference(
        prop: KProperty1<E, T>,
        mappedBy: KProperty1<T, *>,
        block: (MappedAssociationDSL.() -> Unit)? = null
    ) {
        val modelProp = builder.inverseProp(prop, mappedBy) as ModelPropImpl
        if (!modelProp.isReference) {
            throw ModelException("Cannot map '${prop}' as mapped reference property")
        }
        if (block !== null) {
            MappedAssociationDSL(modelProp).block()
        }
    }

    fun <T: Entity<*>> mappedList(
        prop: KProperty1<E, List<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedAssociationDSL.() -> Unit)? = null
    ) {
        val modelProp = builder.inverseProp(prop, mappedBy) as ModelPropImpl
        if (!modelProp.isList) {
            throw ModelException("Cannot map '${prop}' as mapped list property")
        }
        if (block !== null) {
            MappedAssociationDSL(modelProp).block()
        }
    }

    fun <T: Entity<*>> mappedConnection(
        prop: KProperty1<E, Connection<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedAssociationDSL.() -> Unit)? = null
    ) {
        val modelProp = builder.inverseProp(prop, mappedBy) as ModelPropImpl
        if (!modelProp.isReference) {
            throw ModelException("Cannot map '${prop}' as mapped connection property")
        }
        if (block !== null) {
            MappedAssociationDSL(modelProp).block()
        }
    }

    fun <T> userImplementation(
        prop: KProperty1<E, T>,
        block: (UserImplementationDSL.() -> Unit)? = null
    ) {
        val modelProp = builder.transientProp(prop) as ModelPropImpl
        if (block !== null) {
            UserImplementationDSL(modelProp).block()
        }
    }
}
