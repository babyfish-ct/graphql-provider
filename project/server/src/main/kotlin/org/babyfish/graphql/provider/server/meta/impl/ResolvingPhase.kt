package org.babyfish.graphql.provider.server.meta.impl

enum class ResolvingPhase {
    SUPER_TYPE,
    DECLARED_PROPS,
    PROPS,
    PROP_DETAIL,
    ID_PROP,
}