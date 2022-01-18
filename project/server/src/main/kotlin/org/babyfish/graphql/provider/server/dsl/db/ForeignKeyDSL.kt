package org.babyfish.graphql.provider.server.dsl.db

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.OnDeleteAction
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class ForeignKeyDSL internal constructor(
    private val column: EntityPropImpl.ColumnImpl
) {
    var columnName: String? by column::userName
    var onDelete: OnDeleteAction by column::onDelete
}