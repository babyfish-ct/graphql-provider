package org.babyfish.graphql.provider.runtime.cfg

import io.r2dbc.spi.ConnectionFactory
import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.runtime.createSqlClientByEntityMappers
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.JdbcExecutor
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.transaction.ReactiveTransactionManager

@Configuration
open class KimmerSQLAutoConfiguration(
    private val mappers: List<EntityMapper<*, *>>,
    private val scalarProviders: List<ScalarProvider<*, *>>
) {

    @Bean
    open fun sqlClient(
        jdbcExecutor: JdbcExecutor?,
        r2dbcExecutor: R2dbcExecutor?,
        dialect: Dialect?
    ): SqlClient =
        createSqlClientByEntityMappers(
            mappers,
            scalarProviders,
            jdbcExecutor,
            r2dbcExecutor,
            dialect
        )

    @Bean
    open fun r2dbcClient(
        sqlClient: SqlClient,
        databaseClient: DatabaseClient
    ): R2dbcClient =
        R2dbcClient(sqlClient, databaseClient)

    @ConditionalOnMissingBean(ReactiveAuthenticationManager::class)
    @Bean
    open fun reactiveTransactionManager(
        connectionFactory: ConnectionFactory
    ): ReactiveTransactionManager =
        R2dbcTransactionManager(connectionFactory)
}
