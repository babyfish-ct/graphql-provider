package org.babyfish.graphql.provider.server.cfg.db

import org.babyfish.graphql.provider.server.cfg.GraphQLProviderConfiguration
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderConfiguration
class MiddleTableConfiguration internal constructor(
    private val middleTable: EntityPropImpl.MiddleTableImpl
){
    var tableName: String? by middleTable::userTableName
    var joinColumnName: String? by middleTable::userJoinColumnName
    var targetJoinColumnName: String? by middleTable::userTargetJoinColumnName
}