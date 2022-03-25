package org.babyfish.graphql.provider.starter.dgs

import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderAutoConfiguration
import org.babyfish.graphql.provider.runtime.cfg.KimmerSQLAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [
    KimmerSQLAutoConfiguration::class,
    GraphQLProviderAutoConfiguration::class,
    DynamicCodeRegistry::class,
    DynamicTypeDefinitions::class,
    ScalarRegistration::class
])
open class DgsAutoConfiguration