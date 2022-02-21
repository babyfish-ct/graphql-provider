package org.babyfish.graphql.provider.dsl.db

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.kimmer.sql.meta.config.OnDeleteAction

@GraphQLProviderDSL
class ForeignKeyDSL internal constructor(
) {
    var columnName: String? = null
    var onDelete: OnDeleteAction = OnDeleteAction.NONE
}