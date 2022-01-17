package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.server.cfg.db.AssociationDbConfiguration
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.meta.*
import org.babyfish.graphql.provider.server.meta.EntityPropImpl
import java.lang.IllegalStateException

@GraphQLProviderConfiguration
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

@GraphQLProviderConfiguration
class AssociationConfiguration<E, T: Immutable> internal constructor(
    entityProp: EntityPropImpl
): AbstractAssociationConfiguration<E, T>(entityProp) {

    fun db(block: AssociationDbConfiguration.() -> Unit) {}
}

@GraphQLProviderConfiguration
class MappedAssociationConfiguration<E, T: Immutable> internal constructor(
    private val entityProp: EntityPropImpl
): AbstractAssociationConfiguration<E, T>(entityProp)



