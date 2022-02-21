package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.spi.createSqlClient
import kotlin.reflect.KClass

interface ModelClient : SqlClient {

    val queryType: QueryType

    // TODO: kimmer-sql: entityTypeMap -> typeMap
    override val entityTypeMap: Map<KClass<out Entity<*>>, ModelType>
}
