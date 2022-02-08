package org.babyfish.graphql.provider.starter.meta.impl

internal enum class ResolvingPhase {
    SUPER_TYPE,
    DECLARED_PROPS,
    PROPS,
    PROP_FILTER,
    PROP_TARGET,
    PROP_MAPPED_BY,
    PROP_DEFAULT_COLUMN,
    ID_PROP,
}