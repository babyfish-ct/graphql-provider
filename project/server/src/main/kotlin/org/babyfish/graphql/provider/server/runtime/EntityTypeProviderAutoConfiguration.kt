package org.babyfish.graphql.provider.server.runtime

import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean

@ConditionalOnBean(EntityTypeResolver::class)
@GraphQLProviderDSL
open class EntityTypeProviderAutoConfiguration {

    @Bean
    internal open fun entityMetadataResolver(
        queryServices: List<QueryService>,
        assemblers: List<EntityAssembler<*>>
    ) = EntityTypeResolver(queryServices, assemblers)
}