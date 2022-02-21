package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.meta.FilterRedisDependency

internal class FilterRedisDependencyImpl internal constructor(): FilterRedisDependency {

    var dependencyMap = mutableMapOf<String, FilterRedisDependencyImpl>()

    override val prop: ModelProp
        get() = TODO()

    override val dependencies: Collection<FilterRedisDependency>
        get() = dependencyMap.values
}