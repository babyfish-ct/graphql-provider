package org.babyfish.graphql.provider.security.cfg

import org.springframework.stereotype.Component
import org.springframework.web.reactive.config.CorsRegistry

import org.springframework.web.reactive.config.WebFluxConfigurer

@Component
open class CorsConfigurer : WebFluxConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/graphql")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*")
    }
}