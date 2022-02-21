package org.babyfish.graphql.provider.meta

interface FilterRedisDependency {
    val prop: ModelProp
    val dependencies: Collection<FilterRedisDependency>
}