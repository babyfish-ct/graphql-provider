package org.babyfish.graphql.provider.dsl.db

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL

@GraphQLProviderDSL
class EntityTypeDatabaseDSL internal constructor() {
    var tableName: String? = null
}