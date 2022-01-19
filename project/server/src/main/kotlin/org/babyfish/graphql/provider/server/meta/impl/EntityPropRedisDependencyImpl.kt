package org.babyfish.graphql.provider.server.meta.impl

import org.babyfish.graphql.provider.server.meta.EntityProp
import org.babyfish.graphql.provider.server.meta.EntityPropRedisDependency

internal class EntityPropRedisDependencyImpl internal constructor(): EntityPropRedisDependency {

    var dependencyMap = mutableMapOf<String, EntityPropRedisDependencyImpl>()

    override val prop: EntityProp
        get() = TODO()

    override val dependencies: Collection<EntityPropRedisDependency>
        get() = dependencyMap.values
}