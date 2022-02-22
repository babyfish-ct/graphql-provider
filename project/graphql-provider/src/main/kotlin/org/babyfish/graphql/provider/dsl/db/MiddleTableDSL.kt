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
    var tableName: String = ""
    var joinColumnName: String = ""
    var targetJoinColumnName: String = ""
    internal fun create(): MiddleTable =
        MiddleTable(
            tableName = tableName.takeIf { it.isNotEmpty() }
                ?: throw ModelException("tableName of middle table must be specified"),
            joinColumnName = joinColumnName.takeIf { it.isNotEmpty() }
                ?: throw ModelException("joinColumnName of middle table must be specified"),
            targetJoinColumnName = targetJoinColumnName.takeIf { it.isNotEmpty() }
                ?: throw ModelException("targetJoinColumnName of middle table must be specified")
        )
}