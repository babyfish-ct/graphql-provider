package org.babyfish.graphql.provider.example

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import org.babyfish.graphql.provider.example.model.Gender
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.meta.enumProviderByString
import org.babyfish.kimmer.sql.runtime.DefaultR2dbcExecutor
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@SpringBootApplication
class ExampleApplication {

	@Bean
	fun genderProvider() =
		enumProviderByString(Gender::class) {
			map(Gender.MALE, "M")
			map(Gender.FEMALE, "F")
		}

	@Bean
	fun connectionFactoryInitializer(
		connectionFactory: ConnectionFactory
	): ConnectionFactoryInitializer =
		ConnectionFactoryInitializer().apply {
			setConnectionFactory(connectionFactory)
			setDatabasePopulator(ResourceDatabasePopulator(ClassPathResource("data.sql")))
			afterPropertiesSet()
		}

	// Debug
	@Bean
	fun r2dbcExecutor(): R2dbcExecutor =
		object : R2dbcExecutor {
			override suspend fun <R> execute(
				con: Connection,
				sql: String,
				variables: List<Any>,
				block: suspend Result.() -> R
			): R {
				LOGGER.info("SQL: $sql")
				LOGGER.info("variables: $variables")
				return DefaultR2dbcExecutor.execute(con, sql, variables, block)
			}
		}
}

private val LOGGER = LoggerFactory.getLogger(ExampleApplication::class.java)

fun main(args: Array<String>) {
	runApplication<ExampleApplication>(*args)
}
