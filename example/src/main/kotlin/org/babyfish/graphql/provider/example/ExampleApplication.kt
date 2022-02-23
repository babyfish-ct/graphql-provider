package org.babyfish.graphql.provider.example

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@SpringBootApplication
class ExampleApplication {

	@Bean
	fun connectionFactoryInitializer(
		connectionFactory: ConnectionFactory
	): ConnectionFactoryInitializer =
		ConnectionFactoryInitializer().apply {
			setConnectionFactory(connectionFactory)
			setDatabasePopulator(ResourceDatabasePopulator(ClassPathResource("data.sql")))
			afterPropertiesSet()
		}
}

fun main(args: Array<String>) {
	runApplication<ExampleApplication>(*args)
}
