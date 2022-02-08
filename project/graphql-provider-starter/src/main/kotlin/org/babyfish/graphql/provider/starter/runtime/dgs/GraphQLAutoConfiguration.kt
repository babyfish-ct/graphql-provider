package org.babyfish.graphql.provider.starter.runtime.dgs

import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.meta.MetaProvider
import org.babyfish.graphql.provider.starter.runtime.DataFetchers
import org.babyfish.graphql.provider.starter.runtime.GraphQLTypeGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@ComponentScan(basePackageClasses = [GraphQLAutoConfiguration::class])
open class GraphQLAutoConfiguration(
    private val queries: List<Query>,
    private val mappers: List<EntityMapper<*>>
) {
    @Bean
    open fun metaProvider(): MetaProvider =
        GraphQLTypeGenerator(queries, mappers).let {
            it.generate()
            MetaProvider(it.queryType, it.entityTypes)
        }

    @Bean
    internal open fun dataFetchers(): DataFetchers =
        DataFetchers()
}