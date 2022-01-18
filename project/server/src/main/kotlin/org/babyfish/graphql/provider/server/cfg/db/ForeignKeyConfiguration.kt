package org.babyfish.graphql.provider.server.cfg.db

import org.babyfish.graphql.provider.server.cfg.GraphQLProviderConfiguration
import org.babyfish.graphql.provider.server.meta.OnDeleteAction
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderConfiguration
class ForeignKeyConfiguration internal constructor(
    private val column: EntityPropImpl.ColumnImpl
) {
    var columnName: String? by column::userName
    var onDelete: OnDeleteAction by column::onDelete
}