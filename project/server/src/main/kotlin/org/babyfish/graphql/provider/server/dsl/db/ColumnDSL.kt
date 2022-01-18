package org.babyfish.graphql.provider.server.dsl.db

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class ColumnDSL<T> internal constructor(
    column: EntityPropImpl.ColumnImpl
) : AbstractColumnDSL<T>(column) {

    var nullable: Boolean by column::nullable
}