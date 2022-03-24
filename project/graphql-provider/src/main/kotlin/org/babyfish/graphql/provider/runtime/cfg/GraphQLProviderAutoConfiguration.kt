package org.babyfish.graphql.provider.runtime.cfg

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.runtime.*
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import kotlin.reflect.KClass

@Configuration
@EnableConfigurationProperties(GraphQLProviderProperties::class)
open class GraphQLProviderAutoConfiguration(
    private val queries: List<Query>,
    private val mutations: List<Mutation>,
    private val inputMappers: List<InputMapper<*, *>>
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
    open fun dataFetchers(
        r2dbcClient: R2dbcClient,
        argumentsConverter: ArgumentsConverter,
        ctx: ApplicationContext,
        cfg: GraphQLProviderProperties
    ): DataFetchers =
        DataFetchers(r2dbcClient, argumentsConverter, ctx, cfg)

    @Bean
    open fun argumentsConverter(
        metaProvider: MetaProvider
    ): ArgumentsConverter =
        ArgumentsConverter(metaProvider.rootImplicitInputTypeMap)
}