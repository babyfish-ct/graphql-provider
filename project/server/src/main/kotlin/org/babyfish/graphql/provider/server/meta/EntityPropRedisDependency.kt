package org.babyfish.graphql.provider.server.meta

interface EntityPropRedisDependency {
    val prop: EntityProp
    val dependencies: Collection<EntityPropRedisDependency>
}