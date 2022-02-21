package org.babyfish.graphql.provider.dsl.db

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.spi.databaseIdentifier

@GraphQLProviderDSL
class MiddleTableDSL internal constructor(
    private val modelProp: ModelProp,
) {
    var tableName: String? = null
    var joinColumnName: String? = null
    var targetJoinColumnName: String? = null
    internal fun create(): MiddleTable =
        MiddleTable(
            tableName = tableName?.takeIf { it.isNotEmpty() }
                ?: "${databaseIdentifier(modelProp.declaringType.tableName)}_${databaseIdentifier(modelProp.targetType!!.tableName)}_MAPPING",
            joinColumnName = joinColumnName?.takeIf { it.isNotEmpty() }
                ?: "${databaseIdentifier(modelProp.declaringType.tableName)}_ID",
            targetJoinColumnName = targetJoinColumnName?.takeIf { it.isNotEmpty() }
                ?: "${databaseIdentifier(modelProp.targetType!!.tableName)}_ID"
        )
}