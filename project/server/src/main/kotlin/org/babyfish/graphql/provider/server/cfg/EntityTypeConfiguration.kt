package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.server.cfg.db.EntityTypeDbConfiguration
import org.babyfish.graphql.provider.server.cfg.graphql.EntityTypeGraphQLConfiguration
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl
import org.babyfish.graphql.provider.server.meta.EntityProp
import org.babyfish.graphql.provider.server.meta.MetadataException
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import kotlin.reflect.KProperty1

@GraphQLProviderConfiguration
class EntityTypeConfiguration<E: Any> internal constructor(
    private val entityType: EntityTypeImpl
) {

    fun db(block: EntityTypeDbConfiguration.() -> Unit) {
        EntityTypeDbConfiguration(this.entityType.database).block()
    }

    fun redis(enabled: Boolean) {
        this.entityType.redis.enabled = enabled
    }

    fun graphql(block: EntityTypeGraphQLConfiguration.() -> Unit) {
        EntityTypeGraphQLConfiguration(this.entityType.graphql).block()
    }

    fun <T> id(prop: KProperty1<E, T>, block: (IdConfiguration<T>.() -> Unit)? = null) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, EntityProp.Category.ID, prop)
        block?.let {
            IdConfiguration<T>(entityProp).it()
        }
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T> scalar(prop: KProperty1<E, T>, block: ScalarConfiguration<T>.() -> Unit) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, EntityProp.Category.SCALAR, prop)
        ScalarConfiguration<T>(entityProp).block()
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> reference(prop: KProperty1<E, T?>, block: AssociationConfiguration<E, T>.() -> Unit) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, EntityProp.Category.REFERENCE, prop)
        AssociationConfiguration<E, T>(entityProp).block()
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> list(prop: KProperty1<E, List<T>>, block: AssociationConfiguration<E, T>.() -> Unit) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, EntityProp.Category.LIST, prop)
        AssociationConfiguration<E, T>(entityProp).block()
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> connection(
        prop: KProperty1<E, out Connection<T>>,
        block: AssociationConfiguration<E, T>.() -> Unit
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, EntityProp.Category.CONNECTION, prop)
        AssociationConfiguration<E, T>(entityProp).block()
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> mappedReference(
        prop: KProperty1<E, T>,
        mappedBy: KProperty1<T, *>,
        block: (MappedAssociationConfiguration<E, T>.() -> Unit)? = null
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, EntityProp.Category.REFERENCE, prop, mappedBy)
        block?.let {
            MappedAssociationConfiguration<E, T>(entityProp).it()
        }
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> mappedList(
        prop: KProperty1<E, List<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedAssociationConfiguration<E, T>.() -> Unit)? = null
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, EntityProp.Category.LIST, prop, mappedBy)
        block?.let {
            MappedAssociationConfiguration<E, T>(entityProp).it()
        }
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T: Immutable> mappedConnection(
        prop: KProperty1<E, out Connection<T>>,
        mappedBy: KProperty1<T, *>,
        block: (MappedAssociationConfiguration<E, T>.() -> Unit)? = null
    ) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, EntityProp.Category.CONNECTION, prop, mappedBy)
        block?.let {
            MappedAssociationConfiguration<E, T>(entityProp).it()
        }
        entityType.declaredProps[prop.name] = entityProp
    }

    fun <T> computed(prop: KProperty1<E, T>, block: ComputedConfiguration<E, T>.() -> Unit) {
        validateProp(prop)
        val entityProp = EntityPropImpl(entityType, EntityProp.Category.COMPUTED, prop)
        block?.let {
            ComputedConfiguration<E, T>(entityProp).it()
        }
        entityType.declaredProps[prop.name] = entityProp
    }

    private fun validateProp(prop: KProperty1<*, *>) {
        entityType.declaredProps[prop.name]?.let {
            if (it.kotlinProp === prop) {
                throw MetadataException("'$prop' cannot be configured twice")
            } else {
                throw MetadataException("Conflict properties: '${prop}' and '$it'")
            }
        }
    }
}

