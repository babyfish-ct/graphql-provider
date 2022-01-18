package org.babyfish.graphql.provider.server

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean

@ConditionalOnBean(EntityTypeProvider::class)
@GraphQLProviderDSL
open class EntityTypeProviderAutoConfiguration {

    @Bean
    open fun entityMetadataProvider(
        assemblers: List<EntityAssembler<*>>
    ) = EntityTypeProvider(assemblers)
}