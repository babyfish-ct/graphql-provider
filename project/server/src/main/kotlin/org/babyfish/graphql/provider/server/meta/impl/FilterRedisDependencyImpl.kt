package org.babyfish.graphql.provider.server.meta.impl

import org.babyfish.graphql.provider.server.meta.EntityProp
import org.babyfish.graphql.provider.server.meta.FilterRedisDependency

internal class FilterRedisDependencyImpl internal constructor(): FilterRedisDependency {

    var dependencyMap = mutableMapOf<String, FilterRedisDependencyImpl>()

    override val prop: EntityProp
        get() = TODO()

    override val dependencies: Collection<FilterRedisDependency>
        get() = dependencyMap.values
}