package com.babyfish.graphql.provider

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.r2dbc.core.DatabaseClient

@EnableR2dbcRepositories
@Configuration
open class AppConfiguration {

    @Bean
    open fun connectionFactoryInitializer(
        connectionFactory: ConnectionFactory
    ): ConnectionFactoryInitializer =
        ConnectionFactoryInitializer().apply {
            setConnectionFactory(connectionFactory)
            setDatabasePopulator(ResourceDatabasePopulator(ClassPathResource("data.sql")))
            afterPropertiesSet()
        }

    @Bean
    open fun databaseClient(connectionFactory: ConnectionFactory) =
        DatabaseClient.create(connectionFactory)

    @Bean
    open fun connectionFactory() =
        ConnectionFactories.get("r2dbc:h2:mem:///r2dbc_db")
}