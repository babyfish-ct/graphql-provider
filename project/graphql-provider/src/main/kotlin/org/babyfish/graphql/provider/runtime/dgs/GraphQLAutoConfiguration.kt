package org.babyfish.graphql.provider.runtime.dgs

import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.runtime.DataFetchers
import org.babyfish.graphql.provider.runtime.createMetaProvider
import org.babyfish.graphql.provider.runtime.createSqlClientByEntityMappers
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.JdbcExecutor
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import kotlin.reflect.KClass

@Configuration
@ComponentScan(basePackageClasses = [GraphQLAutoConfiguration::class])
open class GraphQLAutoConfiguration(
    private val queries: List<Query>,
    private val mappers: List<org.babyfish.graphql.provider.EntityMapper<*, *>>
) {
    @Bean
    open fun sqlClient(
        jdbcExecutor: JdbcExecutor?,
        r2dbcExecutor: R2dbcExecutor?,
        dialect: Dialect?
    ): SqlClient =
        createSqlClientByEntityMappers(mappers, jdbcExecutor, r2dbcExecutor, dialect)

    @Suppress("UNCHECKED_CAST")
    @Bean
    open fun metaProvider(
        sqlClient: SqlClient
    ): MetaProvider =
        createMetaProvider(
            queries,
            sqlClient.entityTypeMap as Map<KClass<out Entity<*>>, ModelType>
        )

    @Bean
    internal open fun dataFetchers(): DataFetchers =
        DataFetchers()
}