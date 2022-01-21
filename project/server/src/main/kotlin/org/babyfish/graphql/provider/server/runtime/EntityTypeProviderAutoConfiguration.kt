package org.babyfish.graphql.provider.server.runtime

import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean

@ConditionalOnBean(EntityTypeProvider::class)
@GraphQLProviderDSL
open class EntityTypeProviderAutoConfiguration {

    @Bean
    open fun entityMetadataProvider(
        queryServices: List<QueryService>,
        assemblers: List<EntityAssembler<*>>
    ) = EntityTypeProvider(queryServices, assemblers)
}