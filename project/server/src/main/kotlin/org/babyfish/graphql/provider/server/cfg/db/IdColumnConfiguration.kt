package org.babyfish.graphql.provider.server.cfg.db

import org.babyfish.graphql.provider.server.meta.EntityPropImpl

class IdColumnConfiguration<T> internal constructor(
    column: EntityPropImpl.ColumnImpl
) : AbstractColumnConfiguration<T>(column)