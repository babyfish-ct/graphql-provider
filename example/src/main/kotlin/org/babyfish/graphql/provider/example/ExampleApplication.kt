package org.babyfish.graphql.provider.example

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import org.babyfish.graphql.provider.example.model.Gender
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
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.*

@SpringBootApplication
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableTransactionManagement
class ExampleApplication {

	@Bean
	fun dialect() =
		org.babyfish.kimmer.sql.runtime.dialect.H2Dialect()

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
			setDatabasePopulator(
				ResourceDatabasePopulator(
					ClassPathResource("security.sql"),
					ClassPathResource("business.sql")
				)
			)
			afterPropertiesSet()
		}

	// Debug
	@Bean
	fun r2dbcExecutor(): R2dbcExecutor =
		object : R2dbcExecutor {
			override suspend fun <R> execute(
				con: Connection,
				sql: String,
				variables: Collection<Any>,
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
	println(TimeZone.getDefault())
	runApplication<ExampleApplication>(*args)
}
