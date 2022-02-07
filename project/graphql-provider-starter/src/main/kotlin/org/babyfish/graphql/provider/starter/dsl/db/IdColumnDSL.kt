package org.babyfish.graphql.provider.starter.dsl.db

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class IdColumnDSL<T> internal constructor(
    column: EntityPropImpl.ColumnImpl
) : AbstractColumnDSL<T>(column)