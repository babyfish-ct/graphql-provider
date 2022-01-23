package org.babyfish.graphql.provider.server.runtime

import org.babyfish.graphql.provider.server.EntityMapper
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean

@ConditionalOnBean(EntityTypeParser::class)
@GraphQLProviderDSL
open class GraphQLAutoConfiguration {

    @Bean
    internal open fun entityMetadataResolver(
        queryServices: List<QueryService>,
        assemblers: List<EntityMapper<*>>
    ) = EntityTypeParser(queryServices, assemblers)
}