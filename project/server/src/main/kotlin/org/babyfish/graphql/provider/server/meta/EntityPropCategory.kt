package org.babyfish.graphql.provider.server.meta

enum class EntityPropCategory {
    ID,
    SCALAR,
    REFERENCE,
    LIST,
    CONNECTION,
    MAPPED_REFERENCE,
    MAPPED_LIST,
    MAPPED_CONNECTION,
    COMPUTED
}