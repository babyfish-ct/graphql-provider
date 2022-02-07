package org.babyfish.graphql.provider.starter.runtime.dgs

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [GraphQLAutoConfiguration::class])
open class GraphQLAutoConfiguration