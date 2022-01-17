package org.babyfish.graphql.provider.server

import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.cfg.EntityConfiguration
import org.babyfish.graphql.provider.server.meta.EntityTypeImpl
import org.babyfish.graphql.provider.server.meta.EntityType
import org.springframework.core.GenericTypeResolver
import java.lang.IllegalStateException
import kotlin.reflect.KClass

class EntityTypeProvider(
    assemblers: List<EntityAssembler<*>>
) {
    private val entityTypeMap: Map<Class<*>, EntityType>

    init {
        val map = mutableMapOf<Class<*>, EntityTypeImpl>()
        for (assembler in assemblers) {
            val entityClass = GenericTypeResolver.resolveTypeArgument(
                assembler::class.java,
                EntityAssembler::class.java
            ) as Class<out Immutable>
            if (map.containsKey(entityClass)) {
                throw IllegalStateException("Duplicated entity assemblers for entity type '${entityClass.name}'")
            }
            val entityImpl = EntityTypeImpl(entityClass.kotlin)
            (assembler as EntityAssembler<Immutable>).apply {
                EntityConfiguration<Immutable>(entityImpl).assemble()
            }
            map[entityClass] = entityImpl
        }
        entityTypeMap = map
        for (entityType in map.values) {
            entityType.resolve(this, EntityTypeImpl.ResolvingPhase.SUPER_TYPE)
        }
        for (entityType in map.values) {
            entityType.resolve(this, EntityTypeImpl.ResolvingPhase.DECLARED_PROPS)
        }
        for (entityType in map.values) {
            entityType.resolve(this, EntityTypeImpl.ResolvingPhase.PROPS)
        }
        for (entityType in map.values) {
            entityType.resolve(this, EntityTypeImpl.ResolvingPhase.PROP_DETAIL)
        }
    }

    operator fun get(type: KClass<out Immutable>): EntityType =
        entityTypeMap[type.java] ?: error("No entity assembler for entity type '${type}'")

    fun tryGet(type: KClass<out Immutable>): EntityType? =
        entityTypeMap[type.java]
}