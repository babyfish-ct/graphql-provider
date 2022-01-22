package org.babyfish.graphql.provider.server.runtime

import graphql.schema.GraphQLSchema
import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl
import org.babyfish.graphql.provider.server.meta.impl.ResolvingPhase
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.meta.ImmutableType
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.KClass

internal class EntityTypeResolver(
    queryServices: List<QueryService>,
    assemblers: List<EntityAssembler<*>>
) {
    private val entityTypeMap = mutableMapOf<Class<*>, EntityTypeImpl>()

    val schema: GraphQLSchema

    init {
        for (assembler in assemblers) {
            val type = GenericTypeResolver.resolveTypeArgument(
                assembler::class.java,
                EntityAssembler::class.java
            ) as Class<out Immutable>
            (assembler as EntityAssembler<Immutable>).apply {
                EntityTypeDSL<Immutable>(get(type.kotlin)).assemble()
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
        schema = ExecutableSchemaGenerator(queryServices, entityTypeMap.values).generate()
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
