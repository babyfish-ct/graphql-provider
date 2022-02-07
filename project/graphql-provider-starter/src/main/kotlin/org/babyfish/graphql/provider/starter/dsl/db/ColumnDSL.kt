package org.babyfish.graphql.provider.starter.dsl.db

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class ColumnDSL<T> internal constructor(
    column: EntityPropImpl.ColumnImpl
) : AbstractColumnDSL<T>(column) {

    var nullable: Boolean by column::nullable
}