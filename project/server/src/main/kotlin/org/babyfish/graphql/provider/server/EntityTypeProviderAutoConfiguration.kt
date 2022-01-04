package org.babyfish.graphql.provider.server

import org.babyfish.graphql.provider.server.cfg.Configuration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean

@ConditionalOnBean(EntityTypeProvider::class)
@Configuration
open class EntityTypeProviderAutoConfiguration {

    @Bean
    open fun entityMetadataProvider(
        assemblers: List<EntityAssembler<*>>
    ) = EntityTypeProvider(assemblers)
}