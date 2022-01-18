package org.babyfish.graphql.provider.server.cfg.db

import org.babyfish.graphql.provider.server.cfg.GraphQLProviderConfiguration
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderConfiguration
class IdColumnConfiguration<T> internal constructor(
    column: EntityPropImpl.ColumnImpl
) : AbstractColumnConfiguration<T>(column)