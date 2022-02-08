package org.babyfish.graphql.provider.starter.runtime.dgs

import org.babyfish.graphql.provider.starter.runtime.DataFetchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
@ComponentScan(basePackageClasses = [GraphQLAutoConfiguration::class])
open class GraphQLAutoConfiguration {

    @Bean
    internal open fun dataFetchers(databaseClient: DatabaseClient): DataFetchers =
        DataFetchers(databaseClient)
}