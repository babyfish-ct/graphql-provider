package org.babyfish.graphql.provider.starter.dsl.db

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.starter.meta.OnDeleteAction
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class ForeignKeyDSL internal constructor(
    private val column: EntityPropImpl.ColumnImpl
) {
    var columnName: String? by column::userName
    var onDelete: OnDeleteAction by column::onDelete
}