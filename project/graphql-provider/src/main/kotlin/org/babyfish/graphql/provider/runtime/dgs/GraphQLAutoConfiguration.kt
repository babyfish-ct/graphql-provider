package org.babyfish.graphql.provider.runtime.dgs

import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelClient
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.QueryType
import org.babyfish.graphql.provider.runtime.DataFetchers
import org.babyfish.graphql.provider.runtime.GraphQLTypeGenerator
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.spi.createSqlClient
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
    open fun modelClient(): ModelClient {
        val generator = GraphQLTypeGenerator(queries, mappers)
        val sqlClient = createSqlClient {
            generator.generate(this)
        }
        return ModelClientImpl(sqlClient, generator.queryType)
    }

    @Bean
    internal open fun dataFetchers(): DataFetchers =
        DataFetchers()

    private class ModelClientImpl(
        private val sqlClient: SqlClient,
        override val queryType: QueryType
    ): ModelClient, SqlClient by sqlClient {

        @Suppress("UNCHECKED_CAST")
        override val entityTypeMap: Map<KClass<out Entity<*>>, ModelType>
            get() = sqlClient.entityTypeMap as Map<KClass<out Entity<*>>, ModelType>
    }
}