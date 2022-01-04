package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.server.meta.*
import org.babyfish.graphql.provider.server.meta.EntityPropImpl
import java.lang.IllegalStateException

@Configuration
abstract class AbstractAssociationConfiguration<E, T: Immutable> internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun filter(vararg args: Arg, block: Filter<T>.() -> Unit) {

    }

    fun redis(enabled: Boolean = true, block: (RedisConfiguration<T>.() -> Unit)? = null) {
        if (enabled) {
            val dependencyMap = mutableMapOf<String, EntityPropImpl.RedisDependencyImpl>()
            block?.let {
                RedisConfiguration<T>(dependencyMap).it()
            }
            entityProp.redisDependencyMap = dependencyMap
        } else {
            entityProp.redisDependencyMap = null
            if (block !== null) {
                throw IllegalArgumentException("Cannot specify RedisConfiguration for '${entityProp.kotlinProp.name}' because it's redis configuration is disabled")
            }
        }
    }
}

class AssociationConfiguration<E, T: Immutable> internal constructor(
    entityProp: EntityPropImpl
): AbstractAssociationConfiguration<E, T>(entityProp) {

    fun db(block: AssociationDbConfiguration.() -> Unit) {}
}

class MappedAssociationConfiguration<E, T: Immutable> internal constructor(
    private val entityProp: EntityPropImpl
): AbstractAssociationConfiguration<E, T>(entityProp)

@Configuration
class AssociationDbConfiguration internal constructor(private val entityProp: EntityPropImpl) {

    fun foreignKey(
        columnName: String,
        onDelete: OnDeleteAction = OnDeleteAction.NONE
    ) {
        if (entityProp.middleTable !== null) {
            throw IllegalStateException("Cannot configure foreign key for '${entityProp.kotlinProp.name}' because its middle table has been configured")
        }
        if (entityProp.category !== EntityProp.Category.REFERENCE) {
            throw IllegalStateException("Cannot configure foreign key for '${entityProp.kotlinProp.name}' because its category is not ${EntityProp.Category.REFERENCE}")
        }
        val column = entityProp.ColumnImpl()
        column.userName = columnName
        column.onDelete = onDelete
        entityProp.column = column
    }

    fun middleTable(
        tableName: String,
        joinColumn: String,
        targetJoinColumn: String
    ) {
        if (entityProp.column !== null) {
            throw IllegalStateException("Cannot configure middle table for '${entityProp.kotlinProp.name}' because its foreign key has been configured")
        }
        val middleTable = entityProp.MiddleTableImpl()

    }
}

