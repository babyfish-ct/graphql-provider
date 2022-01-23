package org.babyfish.graphql.provider.server.runtime

import org.babyfish.graphql.provider.server.EntityMapper
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl
import org.babyfish.graphql.provider.server.meta.impl.ResolvingPhase
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.meta.ImmutableType
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.KClass

internal class EntityTypeParser(
    queryServices: List<QueryService>,
    assemblers: List<EntityMapper<*>>
) {
    private val entityTypeMap = mutableMapOf<Class<*>, EntityTypeImpl>()

    init {
        for (assembler in assemblers) {
            val type = GenericTypeResolver.resolveTypeArgument(
                assembler::class.java,
                EntityMapper::class.java
            ) as Class<out Immutable>
            (assembler as EntityMapper<Immutable>).apply {
                val entityType = get(type.kotlin)
                entityType.isAssembled = true
                EntityTypeDSL<Immutable>(entityType).map()
            }
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.SUPER_TYPE)
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.DECLARED_PROPS)
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.PROPS)
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.PROP_DETAIL)
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.ID_PROP)
        }
    }

    operator fun get(type: KClass<out Immutable>): EntityTypeImpl =
        entityTypeMap[type.java] ?: create(type)

    private fun create(type: KClass<out Immutable>): EntityTypeImpl {
        val immutableType = if (Connection::class.java.isAssignableFrom(type.java)) {
            throw IllegalArgumentException(
                "Type '${type.qualifiedName}' cannot be considered as entity " +
                    "because it implements '${Connection::class.qualifiedName}'")
        } else {
            ImmutableType.of(type)
        }
        val entityType = EntityTypeImpl(immutableType)
        entityTypeMap[type.java] = entityType
        for (superType in immutableType.superTypes) {
            this[superType.kotlinType]
        }
        for (prop in immutableType.declaredProps.values) {
            prop.targetType?.kotlinType?.let {
                this[it]
            }
        }
        return entityType
    }
}
