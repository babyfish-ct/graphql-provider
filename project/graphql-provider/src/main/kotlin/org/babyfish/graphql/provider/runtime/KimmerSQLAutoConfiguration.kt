package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.JdbcExecutor
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
abstract class KimmerSQLAutoConfiguration(
    private val mappers: List<EntityMapper<*, *>>
) {

    @Bean
    open fun sqlClient(
        jdbcExecutor: JdbcExecutor?,
        r2dbcExecutor: R2dbcExecutor?,
        dialect: Dialect?
    ): SqlClient =
        createSqlClientByEntityMappers(mappers, jdbcExecutor, r2dbcExecutor, dialect)

    @Bean
    open fun r2dbcClient(
        sqlClient: SqlClient,
        databaseClient: DatabaseClient
    ): R2dbcClient =
        R2dbcClient(sqlClient, databaseClient)
}
