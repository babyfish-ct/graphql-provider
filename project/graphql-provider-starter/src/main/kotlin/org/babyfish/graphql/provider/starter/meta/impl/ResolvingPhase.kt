package org.babyfish.graphql.provider.starter.meta.impl

enum class ResolvingPhase {
    SUPER_TYPE,
    DECLARED_PROPS,
    PROPS,
    PROP_TARGET,
    PROP_MAPPED_BY,
    ID_PROP,
}