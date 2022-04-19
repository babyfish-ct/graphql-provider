package org.babyfish.graphql.provider.runtime.cfg

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.runtime.*
import org.babyfish.graphql.provider.security.AuthenticationExtractor
import org.babyfish.graphql.provider.security.SecurityChecker
import org.babyfish.graphql.provider.security.cfg.SecurityConfiguration
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.transaction.ReactiveTransactionManager
import kotlin.reflect.KClass

@Configuration
@Import(value = [
    SecurityConfiguration::class,
    SecurityConfiguration::class,
    Executor::class,
])
@EnableConfigurationProperties(value = [
    GraphQLProviderProperties::class
])
open class GraphQLProviderAutoConfiguration(
    private val applicationContext: ApplicationContext,
    private val properties: GraphQLProviderProperties,
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
        properties: GraphQLProviderProperties,
        authenticationExtractor: AuthenticationExtractor?,
        securityChecker: SecurityChecker,
        executor: Executor
    ): DataFetchers =
        DataFetchers(
            applicationContext,
            r2dbcClient,
            argumentsConverter,
            properties,
            authenticationExtractor,
            securityChecker,
            executor
        )

    @Bean
    open fun argumentsConverter(
        metaProvider: MetaProvider
    ): ArgumentsConverter =
        ArgumentsConverter(metaProvider.rootImplicitInputTypeMap)

    @Bean
    open fun securityChecker(
        roleHierarchy: RoleHierarchy?
    ): SecurityChecker =
        SecurityChecker(properties, roleHierarchy)
}