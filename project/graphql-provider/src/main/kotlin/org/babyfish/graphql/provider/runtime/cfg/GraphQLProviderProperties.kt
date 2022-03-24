package org.babyfish.graphql.provider.runtime.cfg

import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.meta.ModelType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("graphql.provider")
data class GraphQLProviderProperties(
    val defaultBatchSize: Int = 128,
    val defaultCollectionBatchSize: Int = 16
) {
    fun batchSize(prop: ModelProp): Int =
        prop.batchSize
            ?: if (prop.isConnection || prop.isList) {
                (prop.declaringType as ModelType).graphql.defaultCollectionBatchSize
                    ?: defaultCollectionBatchSize
            } else {
                (prop.declaringType as ModelType).graphql.defaultBatchSize
                    ?: defaultBatchSize
            }
}