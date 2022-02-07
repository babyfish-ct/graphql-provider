package org.babyfish.graphql.provider.starter.dsl.db

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class MiddleTableDSL internal constructor(
    private val middleTable: EntityPropImpl.MiddleTableImpl
){
    var tableName: String? by middleTable::userTableName
    var joinColumnName: String? by middleTable::userJoinColumnName
    var targetJoinColumnName: String? by middleTable::userTargetJoinColumnName
}