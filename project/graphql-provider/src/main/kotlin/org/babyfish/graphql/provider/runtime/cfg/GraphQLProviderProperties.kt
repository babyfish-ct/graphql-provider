package org.babyfish.graphql.provider.runtime.cfg

import org.babyfish.graphql.provider.meta.ModelProp
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.http.HttpHeaders

@ConstructorBinding
@ConfigurationProperties("graphql.provider")
data class GraphQLProviderProperties(
    val defaultBatchSize: Int = 128,
    val defaultCollectionBatchSize: Int = 16,
    val security: Security = Security()
) {
    fun batchSize(prop: ModelProp): Int =
        prop.batchSize
            ?: if (prop.isConnection || prop.isList) {
                prop.declaringType.graphql.defaultCollectionBatchSize
                    ?: defaultCollectionBatchSize
            } else {
                prop.declaringType.graphql.defaultBatchSize
                    ?: defaultBatchSize
            }

    @ConfigurationProperties("graphql.provider.security")
    @ConstructorBinding
    data class Security(
        val httpHeader: String = HttpHeaders.AUTHORIZATION,
        val anonymous: Boolean = true
    )
}