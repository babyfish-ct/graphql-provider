package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.kimmer.Connection
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.server.meta.EntityTypeImpl
import org.babyfish.graphql.provider.server.meta.EntityProp
import org.babyfish.graphql.provider.server.meta.EntityPropImpl
import kotlin.reflect.KProperty1

@Configuration
class EntityConfiguration<E: Any> internal constructor(private val entity: EntityTypeImpl) {

    fun db(block: EntityDbConfiguration.() -> Unit) {
        EntityDbConfiguration(this.entity.database).block()
    }

    fun redis(enabled: Boolean) {
        this.entity.redis.enabled = enabled
    }

    fun graphql(block: EntityGraphQLConfiguration.() -> Unit) {
        EntityGraphQLConfiguration(this.entity.graphql).block()
    }

    fun <T> id(prop: KProperty1<E, T>, block: IdConfiguration<T>.() -> Unit) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entity, EntityProp.Category.ID, prop)
        IdConfiguration<T>(entityProp).block()
        entity.declaredProps[prop.name] = entityProp
    }

    fun <T> scalar(prop: KProperty1<E, T>, block: ScalarConfiguration<T>.() -> Unit) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entity, EntityProp.Category.SCALAR, prop)
        ScalarConfiguration<T>(entityProp).block()
        entity.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> reference(prop: KProperty1<E, T?>, block: AssociationConfiguration<E, T>.() -> Unit) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entity, EntityProp.Category.REFERENCE, prop)
        AssociationConfiguration<E, T>(entityProp).block()
        entity.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> list(prop: KProperty1<E, List<T>>, block: AssociationConfiguration<E, T>.() -> Unit) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entity, EntityProp.Category.LIST, prop)
        AssociationConfiguration<E, T>(entityProp).block()
        entity.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> connection(
        prop: KProperty1<E, out Connection<T>>,
        block: AssociationConfiguration<E, T>.() -> Unit
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entity, EntityProp.Category.CONNECTION, prop)
        AssociationConfiguration<E, T>(entityProp).block()
        entity.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> mappedReference(
        prop: KProperty1<E, T>,
        mappedBy: KProperty1<T, *>,
        block: (MappedAssociationConfiguration<E, T>.() -> Unit)? = null
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entity, EntityProp.Category.REFERENCE, prop, mappedBy)
        block?.let {
            MappedAssociationConfiguration<E, T>(entityProp).it()
        }
        entity.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> mappedList(
        prop: KProperty1<E, List<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedAssociationConfiguration<E, T>.() -> Unit)? = null
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entity, EntityProp.Category.LIST, prop, mappedBy)
        block?.let {
            MappedAssociationConfiguration<E, T>(entityProp).it()
        }
        entity.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> mappedConnection(
        prop: KProperty1<E, out Connection<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedAssociationConfiguration<E, T>.() -> Unit)? = null
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entity, EntityProp.Category.CONNECTION, prop, mappedBy)
        block?.let {
            MappedAssociationConfiguration<E, T>(entityProp).it()
        }
        entity.declaredProps[prop.name] = entityProp
    }

    fun <T> computed(prop: KProperty1<E, T>, block: ComputedConfiguration<E, T>.() -> Unit) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entity, EntityProp.Category.COMPUTED, prop)
        block?.let {
            ComputedConfiguration<E, T>(entityProp).it()
        }
        entity.declaredProps[prop.name] = entityProp
    }

    private fun validateProp(prop: KProperty1<*, *>) {
        if (entity.declaredProps.containsKey(prop.name)) {
            throw IllegalArgumentException("Duplicated configuration for property '${prop.name}'")
        }
    }
}

@Configuration
class EntityDbConfiguration internal constructor(
    private val database: EntityTypeImpl.DatabaseImpl
) {
    fun table(tableName: String) {
        database.userTableName = tableName
    }
}

@Configuration
class EntityGraphQLConfiguration internal constructor(
    private val graphql: EntityTypeImpl.GraphQLImpl
) {

    fun defaultBatchSize(value: Int) {
        graphql.defaultBatchSize = value
    }

    fun defaultCollectionBatchSize(value: Int) {
        graphql.defaultCollectionBatchSize = value
    }
}
