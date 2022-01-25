package org.babyfish.graphql.provider.server.meta.impl

enum class ResolvingPhase {
    SUPER_TYPE,
    DECLARED_PROPS,
    PROPS,
    PROP_TARGET,
    PROP_MAPPED_BY,
    ID_PROP,
}