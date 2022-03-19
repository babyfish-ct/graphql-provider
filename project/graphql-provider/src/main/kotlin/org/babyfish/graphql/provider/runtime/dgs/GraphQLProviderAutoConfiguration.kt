package org.babyfish.graphql.provider.runtime.dgs

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.runtime.DataFetchers
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.runtime.createImplicitInputTypes
import org.babyfish.graphql.provider.runtime.createMetaProvider
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import kotlin.reflect.KClass

@Configuration
@ComponentScan(basePackageClasses = [GraphQLProviderAutoConfiguration::class])
open class GraphQLProviderAutoConfiguration(
    val queries: List<Query>,
    val mutations: List<Mutation>,
    val inputMappers: List<InputMapper<*, *>>
) {
    @Suppress("UNCHECKED_CAST")
    @Bean
    open fun metaProvider(
        sqlClient: SqlClient
    ): MetaProvider {
        val (rootImplicitInputTypeMap, allImplicitInputTypes) =
            createImplicitInputTypes(sqlClient, inputMappers)
        return createMetaProvider(
            queries,
            mutations,
            sqlClient.entityTypeMap as Map<KClass<out Entity<*>>, ModelType>,
            rootImplicitInputTypeMap,
            allImplicitInputTypes
        )
    }

    @Bean
    internal open fun dataFetchers(
        r2dbcClient: R2dbcClient
    ): DataFetchers =
        DataFetchers(r2dbcClient)
}